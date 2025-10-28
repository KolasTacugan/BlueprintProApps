package com.example.blueprintproapps.models

data class MatchResponse2(
    val matchId: String,
    val architectId: String,
    val architectName: String?,
    val architectLocation: String?,
    val architectStyle: String?,
    val architectBudget: String?,
    val architectPhoto: String?,
    val matchStatus: String?
)

data class MatchListResponse(
    val success: Boolean,
    val matches: List<MatchResponse2>
)


