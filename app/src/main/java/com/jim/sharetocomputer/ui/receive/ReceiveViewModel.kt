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

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jim.sharetocomputer.AllOpen
import com.jim.sharetocomputer.WebServerService
import com.jim.sharetocomputer.WebUploadService
import com.jim.sharetocomputer.gateway.WifiApi
import com.jim.sharetocomputer.logging.MyLog

@AllOpen
class ReceiveViewModel(
    val context: Context,
    val wifiApi: WifiApi,
    val navigation: ReceiveNavigation
) : ViewModel() {

    private val isSharing = MutableLiveData(false)
    private val deviceIp = MutableLiveData<String>().apply { value = "unknown" }
    private val isAbleToReceiveData = MediatorLiveData<Boolean>().apply {
        addSource(WebServerService.isRunning) {
            this.value = !it
        }
    }
    private val devicePort = WebUploadService.port

    fun scanQrCode() {
        MyLog.i("Select QrCode")
        navigation.openScanQrCode()
    }

    fun receiveFromComputer() {
        MyLog.i("Select start web")
        navigation.startWebUploadService()
        deviceIp.value = wifiApi.getIp()
        isSharing.value = true
    }

    fun stopWeb() {
        navigation.stopWebUploadService()
        isSharing.value = false
    }

    fun isAbleToReceive() = isAbleToReceiveData

    fun isSharing() = isSharing
    fun deviceIp() = deviceIp
    fun devicePort() = devicePort

}