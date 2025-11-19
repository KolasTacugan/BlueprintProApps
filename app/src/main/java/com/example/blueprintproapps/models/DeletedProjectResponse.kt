package com.example.blueprintproapps.models

data class DeletedProjectResponse(
    val project_Id: String,
    val project_Title: String,
    val clientName: String,
    val deletedDate: String
)

