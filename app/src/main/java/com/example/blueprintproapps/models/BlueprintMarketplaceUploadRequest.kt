package com.example.blueprintproapps.models

data class BlueprintMarketplaceUploadRequest(
    val blueprintName: String,
    val blueprintPrice: String,
    val blueprintDescription: String,
    val blueprintStyle: String,
    val blueprintIsForSale: String = "true"
)
