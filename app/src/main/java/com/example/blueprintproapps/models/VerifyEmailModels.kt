package com.example.blueprintproapps.models

data class VerifyEmailRequest(val email: String)
data class VerifyEmailResponse(val message: String, val success: Boolean, val statusCode: Int)