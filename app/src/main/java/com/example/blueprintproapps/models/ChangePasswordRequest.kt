package com.example.blueprintproapps.models

data class ChangePasswordRequest(
    val email: String,
    val newPassword: String
)
