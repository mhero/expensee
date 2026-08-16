package com.mac.expensee.core.network.api

import com.mac.expensee.core.network.dto.AuthResponseDto
import com.mac.expensee.core.network.dto.LoginRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Future counterpart to local auth; see `feature:auth` `AuthRepository` for how this would slot in. */
interface AuthApi {
    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto
}
