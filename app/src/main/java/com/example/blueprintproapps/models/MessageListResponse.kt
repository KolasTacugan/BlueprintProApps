package com.example.blueprintproapps.models

data class MessageListResponse(
    val success: Boolean,
    val messages: List<MessageResponse>
)
