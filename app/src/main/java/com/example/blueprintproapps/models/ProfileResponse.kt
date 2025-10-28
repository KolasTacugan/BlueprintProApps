package com.example.blueprintproapps.models

data class ProfileApiResponse(
    val message: String?,
    val success: Boolean,
    val statusCode: Int,
    val data: ProfileResponse?
)

data class ProfileResponse(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String?,
    val role: String?,
    val profilePhoto: String?,
    val licenseNo: String?,
    val style: String?,
    val specialization: String?,
    val location: String?,
    val budget: String?,
    val credentialsFilePath: String?,
    val portfolioText: String?,
    val isPro: Boolean
)

