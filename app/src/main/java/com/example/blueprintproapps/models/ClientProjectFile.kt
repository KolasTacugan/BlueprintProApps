package com.example.blueprintproapps.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClientProjectFile(
    val projectFile_Id: Int,
    val project_Id: String,
    val projectFile_fileName: String,
    val projectFile_Path: String,
    val projectFile_Version: Int,
    val projectFile_uploadedDate: String
) : Parcelable



