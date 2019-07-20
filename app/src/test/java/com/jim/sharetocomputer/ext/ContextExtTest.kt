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
import androidx.test.core.app.ApplicationProvider
import com.jim.sharetocomputer.setupNoConnection
import com.jim.sharetocomputer.setupOtherConnection
import com.jim.sharetocomputer.setupVpnConnection
import com.jim.sharetocomputer.setupWifiConnection
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class ContextExtTest {

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }

    @Before
    fun before() {
        application.setupNoConnection()
    }

    @Test
    fun isOnWifi_no_connectivity_manager() {
        @Suppress("DEPRECATION")
        Shadows.shadowOf(application).setSystemService(Context.CONNECTIVITY_SERVICE, null)
        assertEquals(false, application.isOnWifi())
    }

    @Test
    fun isOnWifi_no_network() {
        application.setupNoConnection()
        assertEquals(false, application.isOnWifi())
    }

    @Test
    fun isOnWifi_connected_to_wifi() {
        application.setupWifiConnection()
        assertEquals(true, application.isOnWifi())
    }

    @Test
    fun isOnWifi_connected_to_wifi_and_vpn() {
        application.setupWifiConnection()
        application.setupVpnConnection()
        application.setupOtherConnection()
        assertEquals(true, application.isOnWifi())
    }

}