package com.example.blueprintproapps.models



data class ArchitectSubscriptionRequest(
    val architectId: String
)

data class ArchitectSubscriptionResponse(
    val success: Boolean,
    val sessionId: String?,
    val paymentUrl: String?,
    val totalAmount: Int?,
    val currency: String?
)

data class ArchitectSubscriptionCompleteRequest(
    val architectId: String,
    val sessionId: String? = null // optional
)
