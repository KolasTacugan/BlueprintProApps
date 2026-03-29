package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class ExplainMatchRequest(
    @SerializedName("architectId") val architectId: String,  // camelCase
    @SerializedName("query")       val query: String         // camelCase
)
