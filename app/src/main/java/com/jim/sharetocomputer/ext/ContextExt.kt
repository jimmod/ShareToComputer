package com.jim.sharetocomputer.ext

import android.content.Context
import android.net.Uri
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