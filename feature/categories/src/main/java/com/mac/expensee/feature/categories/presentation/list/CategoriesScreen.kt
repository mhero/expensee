package com.mac.expensee.feature.categories.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mac.expensee.core.ui.theme.Spacing
import com.mac.expensee.core.ui.components.EmptyState
import com.mac.expensee.core.ui.components.ErrorState
import com.mac.expensee.core.ui.components.FullScreenLoading
import com.mac.expensee.core.ui.components.UiState
import com.mac.expensee.feature.categories.domain.model.Category
import org.koin.androidx.compose.koinViewModel

private val PALETTE = listOf(
    "#EF6C00", "#1E88E5", "#8E24AA", "#00897B", "#D81B60", "#43A047", "#FB8C00", "#6D4C41", "#3949AB", "#00ACC1",
)

@Composable
fun CategoriesRoute(
    onNavigateUp: () -> Unit,
    viewModel: CategoriesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    CategoriesScreen(state = state, onAction = viewModel::onAction, onNavigateUp = onNavigateUp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriesScreen(
    state: CategoriesUiState,
    onAction: (CategoriesAction) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryPendingRename by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onAction(CategoriesAction.ErrorShown)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add category")
            }
        },
    ) { padding ->
        when (val categoriesState = state.categories) {
            UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(message = categoriesState.message, modifier = Modifier.padding(padding))
            is UiState.Content -> {
                if (categoriesState.data.isEmpty()) {
                    EmptyState(
                        title = "No categories yet",
                        message = "Tap + to create one.",
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.large),
                    ) {
                        items(categoriesState.data, key = { it.id }) { category ->
                            CategoryRow(
                                category = category,
                                onRename = { categoryPendingRename = category },
                                onDelete = { onAction(CategoriesAction.DeleteCategory(category.id)) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                onAction(CategoriesAction.AddCategory(name, color))
                showAddDialog = false
            },
        )
    }

    categoryPendingRename?.let { category ->
        RenameCategoryDialog(
            initialName = category.name,
            onDismiss = { categoryPendingRename = null },
            onConfirm = { newName ->
                onAction(CategoriesAction.RenameCategory(category.id, newName))
                categoryPendingRename = null
            },
        )
    }
}

@Composable
private fun CategoryRow(category: Category, onRename: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.extraSmall)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(end = Spacing.medium)
                    .clip(CircleShape)
                    .background(categoryColor(category.colorHex))
                    .size(16.dp),
            )
            Text(text = category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onRename) {
                Icon(Icons.Filled.Edit, contentDescription = "Rename ${category.name}")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete ${category.name}")
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PALETTE.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New category") },
        text = {
            androidx.compose.foundation.layout.Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                ColorPicker(selectedColor = selectedColor, onSelect = { selectedColor = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, selectedColor) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun RenameCategoryDialog(initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename category") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }) },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ColorPicker(selectedColor: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.padding(top = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        PALETTE.forEach { hex ->
            val isSelected = hex == selectedColor
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(if (isSelected) 32.dp else 28.dp)
                    .clip(CircleShape)
                    .background(categoryColor(hex))
                    .clickable { onSelect(hex) },
            )
        }
    }
}

private fun categoryColor(hex: String): Color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
    .getOrDefault(Color.Gray)
