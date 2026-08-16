package com.mac.expensee.feature.auth.domain.model

/** An active, authenticated session. Presence of a non-null session is what gates the app's main nav graph. */
data class AuthSession(
    val user: User,
    val token: String,
)
