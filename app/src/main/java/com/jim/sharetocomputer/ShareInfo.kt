package com.jim.sharetocomputer

import com.google.gson.annotations.SerializedName

data class ShareInfo(
    @SerializedName("total")
    val total: Int,

    @SerializedName("files")
    val files: List<FileInfo>
)

data class FileInfo(
    @SerializedName("filename")
    val filename: String
)
