package com.jim.sharetocomputer.ext

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.OpenableColumns
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.logging.MyLog


internal fun Context.getAppName(): String = getString(R.string.app_name)

internal fun Context.getFileName(uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        contentResolver.query(uri, null, null, null, null).use { cursor ->
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
    return result ?: "unknown"
}

internal fun Context.getIp(): String {
    val wifiManager =
        applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
    if (wifiManager == null) {
        MyLog.e("Failed to get phone IP address - WifiManager null")
        return "0.0.0.0"
    }
    val ipAddress = wifiManager.connectionInfo.ipAddress
    val ipAddressFormat = String.format(
        "%d.%d.%d.%d",
        ipAddress and 0xff,
        ipAddress shr 8 and 0xff,
        ipAddress shr 16 and 0xff,
        ipAddress shr 24 and 0xff
    )
    MyLog.i("IP address: $ipAddressFormat")
    return ipAddressFormat
}

internal fun Context.getAppVersionName(): String {
    var v = ""
    try {
        v = packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
    }
    return v
}

internal fun Context.getAppVersionCode(): Long {
    var v = 0L
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            v = packageManager.getPackageInfo(packageName, 0).longVersionCode
        } else {
            @Suppress("DEPRECATION")
            v = packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
    }
    return v
}

internal fun Context.isOnWifi(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.allNetworks.forEach {
        if (connectivityManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) return true
    }
    return false
}

internal fun Context.convertDpToPx(dp: Float): Float {
    return dp * this.resources.displayMetrics.density
}