package com.example.blueprintproapps.models

data class EditProfileResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: ProfileData?
)

data class ProfileData(
    val id: String,
    val user_fname: String,
    val user_lname: String,
    val email: String,
    val phoneNumber: String,
    val user_role: String,
    val user_profilePhoto: String?,
    val user_CredentialsFile: String?
)

