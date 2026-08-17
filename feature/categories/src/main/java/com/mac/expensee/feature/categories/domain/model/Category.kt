package com.mac.expensee.feature.categories.domain.model

data class Category(
    val id: String,
    val name: String,
    val colorHex: String,
    val icon: String,
    val isDefault: Boolean,
)
