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

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowWifiInfo
import java.net.InetAddress

@RunWith(AndroidJUnit4::class)
class WifiApiTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val wifiApi by lazy { WifiApi(application) }
    private val ip = "1.2.3.4"

    @Before
    fun before() {
        val wifiManager =
            ApplicationProvider.getApplicationContext<Application>().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiManagerShadow = Shadows.shadowOf(wifiManager)
        val wifiInfo = ShadowWifiInfo.newInstance()
        val wifiInfoShadow = Shadows.shadowOf(wifiInfo)
        wifiInfoShadow.setInetAddress(InetAddress.getByName(ip))
        wifiManagerShadow.setConnectionInfo(wifiInfo)
    }

    @Test
    fun get_ip() {
        val actualIp = wifiApi.getIp()

        assertEquals(ip, actualIp)
    }

}