package com.example.blueprintproapps.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClientCompliance(
    val compliance_Id: Int?,
    val compliance_Zoning: String?,
    val compliance_Others: String?
) : Parcelable

