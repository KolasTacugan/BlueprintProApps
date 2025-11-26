package com.example.blueprintproapps.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClientProjectFile(

    @SerializedName("fileName")
    val FileName: String,

    @SerializedName("version")
    val Version: Int,

    @SerializedName("uploadedDate")
    val UploadedDate: String,

    @SerializedName("filePath")
    val FilePath: String,

    val projectFile_Id: Int? = null,
    val project_Id: String? = null
) : Parcelable
