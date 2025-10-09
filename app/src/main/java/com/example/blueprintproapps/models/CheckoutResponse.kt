package com.example.blueprintproapps.models

data class CheckoutResponse(
    val sessionId: String,
    val paymentUrl: String,
    val totalAmount: Double,
    val currency: String = "PHP"
)