package com.example.blueprintproapps.models


data class ChatMessagesResponse(
    val success: Boolean,
    val messages: List<MessageResponse>
)
