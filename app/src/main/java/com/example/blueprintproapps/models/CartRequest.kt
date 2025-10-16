package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class CartRequest(
    @SerializedName("ClientId") val clientId: String,
    @SerializedName("BlueprintId") val blueprintId: Int,
    @SerializedName("Quantity") val quantity: Int
)