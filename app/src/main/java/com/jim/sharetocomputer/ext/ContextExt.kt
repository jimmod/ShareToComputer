package com.jim.sharetocomputer.ext

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.OpenableColumns
import com.jim.sharetocomputer.R


internal fun Context.appName(): String = getString(R.string.app_name)

internal fun Context.getFileName(uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        contentResolver.query(uri, null, null, null, null).use {cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result!!.substring(cut + 1)
        }
    }
    return result?:"unknown"
}

internal fun Context.getIp(): String {
    val wifiManager =
        applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager? ?: return "0.0.0.0"
    val ipAddress = wifiManager.connectionInfo.ipAddress
    return String.format(
        "%d.%d.%d.%d",
        ipAddress and 0xff,
        ipAddress shr 8 and 0xff,
        ipAddress shr 16 and 0xff,
        ipAddress shr 24 and 0xff
    )
}

internal fun Context.convertDpToPx(dp: Float): Float {
    return dp * this.resources.displayMetrics.density
}