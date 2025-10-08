package com.example.blueprintproapps.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val role: String? = "Client", // default role if none chosen

    // Architect-specific fields (optional for clients)
    val licenseNo: String? = null,
    val style: String? = null,
    val specialization: String? = null,
    val location: String? = null,
    val laborCost: String? = null,

    // Optional profile info
    val profilePhoto: String? = null,
    val credentialsFile: String? = null
)
