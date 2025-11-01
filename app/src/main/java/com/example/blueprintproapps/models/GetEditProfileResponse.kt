package com.example.blueprintproapps.models

data class GetEditProfileResponse(
    val message: String,
    val success: Boolean,
    val statusCode: Int,
    val data: EditProfileData
)

data class EditProfileData(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val profilePhoto: String?,
    val licenseNo: String?,
    val style: String?,
    val specialization: String?,
    val location: String?,
    val budget: String?,
    val credentialsFile: String?
)
