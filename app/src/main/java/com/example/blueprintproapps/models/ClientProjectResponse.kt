package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class ClientProjectResponse(
    val project_Id: String,
    val project_Title: String,
    val project_Status: String,
    val project_Budget: String?,
    val project_startDate: String,
    val project_endDate: String?,
    val blueprint_Id: Int,
    val blueprint_Name: String,
    val blueprint_ImageUrl: String?,

    val architectName: String,
    val user_architectId: String
)
