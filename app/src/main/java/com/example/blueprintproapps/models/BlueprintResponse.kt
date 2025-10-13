package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class BlueprintResponse(
    //para mapush
    val blueprintId: Int,
    val blueprintName: String,
    val blueprintImage: String?,
    val blueprintPrice: Double,
    val blueprintIsForSale: Boolean
)


