package com.example.blueprintproapps.models

data class MessageResponse(
    val messageId: String,
    val clientId: String,
    val architectId: String,
    val senderId: String,
    val messageBody: String,
    val messageDate: String,
    val isRead: Boolean,
    val attachmentUrl: String?
)
