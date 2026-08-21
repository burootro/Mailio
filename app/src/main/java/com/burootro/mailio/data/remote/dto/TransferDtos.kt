package com.burootro.mailio.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StartTransferRequest(
    val addressId: String
)

@Serializable
data class StartTransferResponse(
    val code: String,
    val email: String,
    val expiresAt: Long,
    val expiresInMinutes: Int = 30
)

@Serializable
data class CancelTransferRequest(
    val addressId: String
)

@Serializable
data class ClaimTransferRequest(
    val code: String
)

@Serializable
data class ClaimTransferResponse(
    val ok: Boolean = true,
    val addressId: String,
    val email: String
)
