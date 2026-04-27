package com.example.blueprintproapps.models

data class ArchitectProjectResponse(
    val project_Id: String,
    val project_Title: String,
    val project_Budget: String?,
    val project_Status: String,
    val blueprint_Id: Int,
    val blueprintImage: String?,
    val clientId: String,
    val clientName: String
)
