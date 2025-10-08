package com.example.blueprintproapps.models

data class LoginResponse(
    val message: String,
    val userId: String,
    val email: String,
    val role: String
)