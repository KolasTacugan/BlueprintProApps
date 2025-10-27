package com.example.blueprintproapps.models

data class ProfileApiResponse(
    val success: Boolean,
    val message: String,
    val data: ProfileData
)

data class ProfileData(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val profileImageUrl: String?,
    val credentialsPdfUrl: String?
)
