package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class ClarificationQuestion(
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>
)

data class MatchesApiResponse(
    @SerializedName("needsClarification")
    val needsClarification: Boolean = false,

    @SerializedName("originalQuery")
    val originalQuery: String? = null,

    @SerializedName("questions")
    val questions: List<ClarificationQuestion>? = null,

    // nullable because this field is absent in the needsClarification=true response
    @SerializedName("matches")
    val matches: List<MatchResponse>? = null,

    @SerializedName("totalArchitects")
    val totalArchitects: Int = 0,

    @SerializedName("outOfScope")
    val outOfScope: Boolean = false,

    @SerializedName("lacksDetails")
    val lacksDetails: Boolean = false,

    @SerializedName("showFeedback")
    val showFeedback: Boolean = false
)
