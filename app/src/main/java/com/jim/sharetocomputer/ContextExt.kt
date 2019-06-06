package com.jim.sharetocomputer

import android.content.Context
import android.net.wifi.WifiManager


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
