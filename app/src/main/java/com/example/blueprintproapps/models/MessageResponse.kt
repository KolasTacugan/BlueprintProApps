package com.example.blueprintproapps.models

data class MessageResponse(
    val messageId: Int,
    val senderId: String,
    val receiverId: String,
    val messageContent: String,
    val timestamp: String,
    val isRead: Boolean
)