package com.jim.sharetocomputer

data class ShareInfo(
    val total: Int,
    val files: List<FileInfo>
)

data class FileInfo(val filename: String)
