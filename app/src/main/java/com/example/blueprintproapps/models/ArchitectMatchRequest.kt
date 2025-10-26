package com.example.blueprintproapps.models

data class ArchitectMatchRequest(
    val matchId: String,
    val clientId: String,
    val clientName: String,
    val matchDate: String,
    val matchStatus: String
)
