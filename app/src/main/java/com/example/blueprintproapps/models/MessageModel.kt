package com.example.blueprintproapps.models



data class MessageModel(
    val text: String,
    val isSent: Boolean // true = sent, false = received
)
