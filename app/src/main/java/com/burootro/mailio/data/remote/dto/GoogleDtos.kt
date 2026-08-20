package com.burootro.mailio.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleSignInRequest(
    val idToken: String
)

@Serializable
data class GoogleProfile(
    val email: String? = null,
    val name: String? = null,
    val photo: String? = null
)

@Serializable
data class GoogleSignInResponse(
    val isNew: Boolean = false,
    val userId: String,
    val accessKey: String,
    val profile: GoogleProfile = GoogleProfile(),
    val domains: List<String> = emptyList(),
    val addresses: List<AddressDto> = emptyList(),
    val messages: List<MessageDto> = emptyList()
)
