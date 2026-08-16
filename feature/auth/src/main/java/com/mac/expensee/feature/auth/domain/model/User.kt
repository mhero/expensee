package com.mac.expensee.feature.auth.domain.model

data class User(
    val id: String,
    val username: String,
    val biometricUnlockEnabled: Boolean,
)
