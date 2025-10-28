package com.example.blueprintproapps.models

data class ArchitectProjectTrackerResponse(
    val projectTrack_Id: Int,
    val project_Id: String,
    val currentFileName: String?,
    val currentFilePath: String?,
    val currentRevision: Int,
    val status: String,
    val architectName: String?,
    val isRated: Boolean,
    val revisionHistory: List<ArchitectProjectFileResponse>,
    val compliance: ArchitectComplianceResponse?,  // ✅ matches server JSON
    val finalizationNotes: String?,
    val projectTrackerStatus: String?
)
