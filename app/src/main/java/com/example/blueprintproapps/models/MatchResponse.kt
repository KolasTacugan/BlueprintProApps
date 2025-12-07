package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class MatchResponse(
    @SerializedName("MatchId") val matchId: String?,
    @SerializedName("ClientId") val clientId: String?,
    @SerializedName("ClientName") val clientName: String?,
    @SerializedName("ArchitectId") val architectId: String,
    @SerializedName("ArchitectName") val architectName: String,
    @SerializedName("ArchitectStyle") val architectStyle: String?,
    @SerializedName("ArchitectLocation") val architectLocation: String?,
    @SerializedName("ArchitectBudget") val architectBudget: String?,
    @SerializedName("MatchStatus") val matchStatus: String,
    @SerializedName("RealMatchStatus") val realMatchStatus: String?,
    @SerializedName("MatchDate") val matchDate: String?
)
