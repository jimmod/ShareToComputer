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

package com.jim.sharetocomputer.ext

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo

@RunWith(RobolectricTestRunner::class)
class ContextExtTest {

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }

    @Before
    fun before() {
        setupNoConnection()
    }

    @Test
    fun isOnWifi_no_network() {
        setupNoConnection()
        assertEquals(false, application.isOnWifi())
    }

    @Test
    fun isOnWifi_connected_to_wifi() {
        setupWifiConnection()
        assertEquals(true, application.isOnWifi())
    }

    @Test
    fun isOnWifi_connected_to_wifi_and_vpn() {
        setupWifiConnection()
        setupVpnConnection()
        assertEquals(true, application.isOnWifi())
    }

    private fun setupNoConnection() {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        Shadows.shadowOf(connectivityManager).clearAllNetworks()
    }

    @Suppress("DEPRECATION")
    private fun setupWifiConnection() {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiNetworkInfo = ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_WIFI,
            0,
            true,
            NetworkInfo.State.CONNECTED
        )
        val network = ShadowNetwork.newInstance(ConnectivityManager.TYPE_WIFI)
        val networkCapabilities = ShadowNetworkCapabilities.newInstance()
        Shadows.shadowOf(networkCapabilities).addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        Shadows.shadowOf(connectivityManager).apply {
            addNetwork(network, wifiNetworkInfo)
            setActiveNetworkInfo(wifiNetworkInfo)
            setNetworkCapabilities(network, networkCapabilities)
        }
    }

    @Suppress("DEPRECATION")
    private fun setupVpnConnection() {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiNetworkInfo = ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_VPN,
            1,
            true,
            NetworkInfo.State.CONNECTED
        )
        val network = ShadowNetwork.newInstance(ConnectivityManager.TYPE_VPN)
        val networkCapabilities = ShadowNetworkCapabilities.newInstance()
        Shadows.shadowOf(networkCapabilities).addTransportType(NetworkCapabilities.TRANSPORT_VPN)
        Shadows.shadowOf(connectivityManager).apply {
            addNetwork(network, wifiNetworkInfo)
            setActiveNetworkInfo(wifiNetworkInfo)
            setNetworkCapabilities(network, networkCapabilities)
        }
    }
}