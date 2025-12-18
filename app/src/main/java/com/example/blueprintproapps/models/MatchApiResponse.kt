package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName


data class MatchesApiResponse(
    @SerializedName("matches")
    val matches: List<MatchResponse>,

    @SerializedName("outOfScope")
    val outOfScope: Boolean,

    @SerializedName("lacksDetails")
    val lacksDetails: Boolean,

    @SerializedName("showFeedback")
    val showFeedback: Boolean
)
