package com.example.blueprintproapps.models

data class ArchitectMatchResponse(
    val matchId: String,
    val clientId: String,
    val clientName: String?,
    val clientLocation: String?,
    val clientStyle: String?,
    val clientBudget: String?,
    val clientPhoto: String?,
    val matchStatus: String?
)

data class ArchitectMatchListResponse(
    val success: Boolean,
    val matches: List<ArchitectMatchResponse>
)
