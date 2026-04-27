package com.example.blueprintproapps.models

data class ClientProfileResponse(
    val success: Boolean,
    val name: String,
    val email: String,
    val phone: String?,
    val profilePhoto: String
)