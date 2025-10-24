package com.example.blueprintproapps.models

data class MessageRequest(
    val clientId: String,
    val architectId: String,
    val senderId: String,
    val messageBody: String
)
