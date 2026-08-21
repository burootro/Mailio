package com.burootro.mailio.data.remote

import com.burootro.mailio.data.remote.dto.AddressListResponse
import com.burootro.mailio.data.remote.dto.CancelTransferRequest
import com.burootro.mailio.data.remote.dto.ClaimTransferRequest
import com.burootro.mailio.data.remote.dto.ClaimTransferResponse
import com.burootro.mailio.data.remote.dto.CreateAddressRequest
import com.burootro.mailio.data.remote.dto.CreateAddressResponse
import com.burootro.mailio.data.remote.dto.GoogleSignInRequest
import com.burootro.mailio.data.remote.dto.GoogleSignInResponse
import com.burootro.mailio.data.remote.dto.MeResponse
import com.burootro.mailio.data.remote.dto.MessageListResponse
import com.burootro.mailio.data.remote.dto.OkResponse
import com.burootro.mailio.data.remote.dto.PushTokenRequest
import com.burootro.mailio.data.remote.dto.RegisterResponse
import com.burootro.mailio.data.remote.dto.RestoreRequest
import com.burootro.mailio.data.remote.dto.RestoreResponse
import com.burootro.mailio.data.remote.dto.StartTransferRequest
import com.burootro.mailio.data.remote.dto.StartTransferResponse
import com.burootro.mailio.data.remote.dto.UpdateLabelRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MailioApi {

    @GET("health")
    suspend fun health(): Map<String, String>

    // ===== تسجيل الدخول =====

    @POST("api/google/signin")
    suspend fun googleSignIn(@Body body: GoogleSignInRequest): GoogleSignInResponse

    @POST("api/auth/register")
    suspend fun register(): RegisterResponse

    @POST("api/auth/restore")
    suspend fun restore(@Body body: RestoreRequest): RestoreResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    // ===== الإشعارات =====

    @POST("api/push/register")
    suspend fun registerPushToken(@Body body: PushTokenRequest): OkResponse

    // ===== نقل العناوين =====

    @POST("api/transfer/start")
    suspend fun startTransfer(@Body body: StartTransferRequest): StartTransferResponse

    @POST("api/transfer/cancel")
    suspend fun cancelTransfer(@Body body: CancelTransferRequest): OkResponse

    @POST("api/transfer/claim")
    suspend fun claimTransfer(@Body body: ClaimTransferRequest): ClaimTransferResponse

    // ===== العناوين =====

    @GET("api/addresses")
    suspend fun listAddresses(): AddressListResponse

    @POST("api/addresses")
    suspend fun createAddress(@Body body: CreateAddressRequest): CreateAddressResponse

    @PATCH("api/addresses/{id}")
    suspend fun updateLabel(
        @Path("id") id: String,
        @Body body: UpdateLabelRequest
    ): OkResponse

    @DELETE("api/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): OkResponse

    @GET("api/addresses/{id}/messages")
    suspend fun getMessages(
        @Path("id") id: String,
        @Query("since") since: Long = 0L,
        @Query("limit") limit: Int = 100
    ): MessageListResponse

    // ===== الرسايل =====

    @GET("api/messages/sync")
    suspend fun syncMessages(
        @Query("since") since: Long = 0L,
        @Query("limit") limit: Int = 200
    ): MessageListResponse

    @DELETE("api/messages/{id}")
    suspend fun deleteMessage(@Path("id") id: String): OkResponse
}
