package com.example.blueprintproapps.models

data class ArchitectProfileResponse(
    val success: Boolean,
    val fullName: String,
    val email: String,
    val phone: String?,
    val photo: String,
    val license: String?,
    val style: String?,
    val specialization: String?,
    val location: String?,
    val credentialsFile: String?
)