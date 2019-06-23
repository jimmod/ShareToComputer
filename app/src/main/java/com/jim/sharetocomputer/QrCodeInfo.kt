package com.jim.sharetocomputer

import com.google.gson.annotations.SerializedName

data class QrCodeInfo(
    @SerializedName("version")
    val version: Int,

    @SerializedName("url")
    val url: String
)