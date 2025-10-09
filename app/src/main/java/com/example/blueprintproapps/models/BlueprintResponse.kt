package com.example.blueprintproapps.models

data class BlueprintResponse(
    val blueprintId: Int,
    val blueprintName: String,
    val blueprintImage: String?,
    val blueprintPrice: Double,
    val blueprintIsForSale: Boolean
)