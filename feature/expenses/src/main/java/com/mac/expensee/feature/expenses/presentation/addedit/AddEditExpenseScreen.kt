package com.mac.expensee.feature.expenses.presentation.addedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mac.expensee.core.ui.theme.Spacing
import com.mac.expensee.feature.expenses.domain.model.ExpenseCategory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddEditExpenseRoute(
    expenseId: String?,
    onDone: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: AddEditExpenseViewModel = koinViewModel { parametersOf(expenseId) },
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) onDone()
    }

    AddEditExpenseScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateUp = onNavigateUp,
    )
}

/** `internal`, not `private`, so `AddEditExpenseScreenTest` (androidTest) can drive it directly with a fixed state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditExpenseScreen(
    state: AddEditExpenseUiState,
    onAction: (AddEditExpenseAction) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { onAction(AddEditExpenseAction.ReceiptPicked(it)) }
    }

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                // Tagged (rather than relying on text alone) because when adding a new expense
                // this title and the save button below render the same text ("Add expense") --
                // see AddEditExpenseScreenTest.
                title = { Text(if (state.isEditing) "Edit expense" else "Add expense", modifier = Modifier.testTag("screenTitle")) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(Spacing.large)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            OutlinedTextField(
                value = state.amountText,
                onValueChange = { onAction(AddEditExpenseAction.AmountChanged(it)) },
                label = { Text("Amount") },
                // Shows which currency this entry will be saved in -- see AddEditExpenseUiState.currency's
                // KDoc: fixed for this form's lifetime, either the user's default-currency setting (new
                // expense) or the currency the expense already had (editing).
                prefix = { Text(state.currency.symbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { onAction(AddEditExpenseAction.DescriptionChanged(it)) },
                label = { Text("Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            CategoryDropdown(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onSelect = { onAction(AddEditExpenseAction.CategorySelected(it)) },
            )

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Date: ${formatDate(state.date)}")
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = { onAction(AddEditExpenseAction.NotesChanged(it)) },
                label = { Text("Notes (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            ReceiptPicker(
                receiptPath = state.receiptPath,
                isUploading = state.isUploadingReceipt,
                onPick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onRemove = { onAction(AddEditExpenseAction.ReceiptRemoved) },
            )

            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { onAction(AddEditExpenseAction.Save) },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveButton"),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(if (state.isEditing) "Save changes" else "Add expense")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onAction(AddEditExpenseAction.DateChanged(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<ExpenseCategory>,
    selectedCategoryId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Select a category"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelect(category.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ReceiptPicker(
    receiptPath: String?,
    isUploading: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    Column {
        Text(text = "Receipt", style = MaterialTheme.typography.labelSmall)
        when {
            isUploading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            receiptPath != null -> Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = File(receiptPath),
                    contentDescription = "Receipt image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                        .clip(RoundedCornerShape(8.dp)),
                )
                IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove receipt")
                }
            }

            else -> OutlinedButton(onClick = onPick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Text(text = "  Attach a receipt")
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMillis))
