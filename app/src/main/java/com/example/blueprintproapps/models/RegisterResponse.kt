package com.example.blueprintproapps.models

data class RegisterResponse(
    val message: String,
    val userId: String,
    val role: String,
    val email: String
)