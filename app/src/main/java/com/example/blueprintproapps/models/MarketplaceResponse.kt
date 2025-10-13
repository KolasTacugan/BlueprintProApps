package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class MarketplaceResponse(
    @SerializedName("blueprints") val Blueprints: List<BlueprintResponse>,
    @SerializedName("stripePublishableKey") val StripePublishableKey: String
)

