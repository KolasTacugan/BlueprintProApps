package com.example.blueprintproapps.models

import com.google.gson.annotations.SerializedName

data class ClientProjectTrackerResponse(
    val projectTrack_Id: Int,
    val project_Id: String,
    val currentFileName: String?,
    val currentFilePath: String?,
    val currentRevision: Int?,
    val status: String,
    val finalizationNotes: String?,
    val compliance: ClientCompliance?,         // MUST BE NULLABLE
    val revisionHistory: List<ClientProjectFile>?, // MUST BE NULLABLE
    val projectStatus: String,
    val isRated: Boolean,
    val architectName: String
)


