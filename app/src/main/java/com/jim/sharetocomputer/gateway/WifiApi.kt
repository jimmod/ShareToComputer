/*
 *     This file is part of Share To Computer  Copyright (C) 2019  Jimmy <https://github.com/jimmod/ShareToComputer>.
 *
 *     Share To Computer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Share To Computer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Share To Computer.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.jim.sharetocomputer.gateway

import android.content.Context
import android.net.wifi.WifiManager
import com.jim.sharetocomputer.AllOpen
import com.jim.sharetocomputer.logging.MyLog

@AllOpen
class WifiApi(val context: Context) {

    fun getIp(): String {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
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

}