/*
    This file is part of Share To Computer  Copyright (C) 2019  Jimmy <https://github.com/jimmod/ShareToComputer>.

    Share To Computer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Share To Computer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Share To Computer.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.jim.sharetocomputer

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.ConsolePrinter
import com.jim.sharetocomputer.coroutines.DirectDispatcher
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo


class RobolectricApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        setupLogging()

        setupCoroutinesDispatchers()

        WebServerService.isRunning.value = false

        setupConnection()
    }

    @Suppress("DEPRECATION")
    private fun setupConnection() {
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
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        Shadows.shadowOf(connectivityManager).apply {
            addNetwork(network, wifiNetworkInfo)
            setActiveNetworkInfo(wifiNetworkInfo)
            setNetworkCapabilities(network, networkCapabilities)
        }
    }

    private fun setupLogging() {
        val config = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .build()

        XLog.init(
            config,
            ConsolePrinter()
        )
    }

    private fun setupCoroutinesDispatchers() {
        TestableDispatchers.setMain(DirectDispatcher())
        TestableDispatchers.setDefault(DirectDispatcher())
        TestableDispatchers.setIo(DirectDispatcher())
        TestableDispatchers.setUnconfined(DirectDispatcher())
    }
}