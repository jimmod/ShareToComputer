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

package com.jim.sharetocomputer.ui.receive

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jim.sharetocomputer.DownloadService
import com.jim.sharetocomputer.QrCodeInfo
import com.jim.sharetocomputer.gateway.WifiApi
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class ReceiveViewModelTest {

    private val navigation: ReceiveNavigation = mock()
    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private val viewModel = ReceiveViewModel(application, WifiApi(application), navigation)

    @Test
    fun scan_qr_code() {
        val qrCodeInfo =
            QrCodeInfo(com.jim.sharetocomputer.Application.QR_CODE_VERSION, "http://localhost:8080")
        runBlocking {
            given(navigation.openScanQrCode()).willReturn(qrCodeInfo)
        }

        viewModel.scanQrCode()

        runBlocking {
            verify(navigation).openScanQrCode()
        }

        val startedService = Shadows.shadowOf(application).peekNextStartedService()

        Assert.assertThat(
            startedService,
            IntentMatchers.hasComponent(DownloadService::class.java.name)
        )
    }

    @Test
    fun scan_qr_code_older_version() {
        val qrCodeInfo = QrCodeInfo(1, "http://localhost:8080")
        runBlocking {
            given(navigation.openScanQrCode()).willReturn(qrCodeInfo)
        }

        viewModel.scanQrCode()

        runBlocking {
            verify(navigation).openScanQrCode()
        }

        val startedService = Shadows.shadowOf(application).peekNextStartedService()

        Assert.assertThat(
            startedService,
            IntentMatchers.hasComponent(DownloadService::class.java.name)
        )
    }

}