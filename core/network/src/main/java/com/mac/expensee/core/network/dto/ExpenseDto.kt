package com.mac.expensee.core.network.dto

import kotlinx.serialization.Serializable

/**
 * Wire format for the (not-yet-implemented) remote API. Deliberately separate from both the
 * Room entity and the domain model: a DTO changes when the API contract changes, an entity
 * changes when local storage changes, and neither should force a change in the other. Mapping
 * between DTO <-> domain lives in each feature's data layer, not here.
 */
@Serializable
data class ExpenseDto(
    val id: String,
    val categoryId: String,
    val amountMinorUnits: Long,
    val currencyCode: String,
    val description: String,
    val notes: String? = null,
    val date: Long,
    val updatedAt: Long,
    val version: Int,
)
