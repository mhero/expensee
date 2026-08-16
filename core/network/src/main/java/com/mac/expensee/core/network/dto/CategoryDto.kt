package com.mac.expensee.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val colorHex: String,
    val icon: String,
    val updatedAt: Long,
    val version: Int,
)
