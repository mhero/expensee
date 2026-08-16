package com.mac.expensee.core.network.api

import com.mac.expensee.core.network.dto.ExpenseDto
import com.mac.expensee.core.network.dto.SyncPullResponseDto
import com.mac.expensee.core.network.dto.SyncPushRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Not called anywhere yet -- there is no backend. This interface exists so [SyncManager] (in
 * `feature:expenses` / a future `core:sync`) can be built against a real Retrofit contract
 * without waiting on a server, and so wiring one up later is "point Retrofit at a real base URL",
 * not "design the client from scratch".
 */
interface ExpenseApi {
    @GET("v1/sync/pull")
    suspend fun pull(@Query("since") sinceEpochMillis: Long): SyncPullResponseDto

    @POST("v1/sync/push")
    suspend fun push(@Body request: SyncPushRequestDto): List<ExpenseDto>
}
