package com.example.blueprintproapps.models

data class ProjectResponse(
    val projectId: Int,
    val projectName: String,
    val projectDescription: String?,
    val projectStatus: String,
    val startDate: String?,
    val endDate: String?,
    val architectName: String?,
    val progressPercentage: Int
)