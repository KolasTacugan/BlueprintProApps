package com.example.blueprintproapps.models

data class ProjectTrackerResponse(
    val projectId: Int,
    val projectName: String,
    val currentStage: String,
    val totalStages: Int,
    val completedStages: Int,
    val progressPercentage: Int,
    val updates: List<ProjectUpdate>
)

data class ProjectUpdate(
    val updateId: Int,
    val stageName: String,
    val description: String?,
    val updateDate: String,
    val isCompleted: Boolean
)