package com.mac.expensee.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class AuthResponseDto(val userId: String, val username: String, val sessionToken: String)
