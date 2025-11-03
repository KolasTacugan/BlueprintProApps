package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class ArchitectComplianceResponse(
    val compliance_Id: Int,

    @SerializedName("compliance_Zoning")
    val zoningFile: String?,

    @SerializedName("compliance_Others")
    val othersFile: String?
)
