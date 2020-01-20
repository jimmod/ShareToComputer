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
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jim.sharetocomputer.*
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.gateway.WifiApi
import com.jim.sharetocomputer.logging.MyLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AllOpen
class ReceiveViewModel(
    val context: Context,
    val wifiApi: WifiApi,
    val navigation: ReceiveNavigation
) : ViewModel() {

    private val isSharing = MutableLiveData<Boolean>().apply { value = false }
    private val deviceIp = MutableLiveData<String>().apply { value = "unknown" }
    private val isAbleToReceiveData = MediatorLiveData<Boolean>().apply {
        addSource(WebServerService.isRunning) {
            this.value = !it
        }
    }
    private val devicePort = WebUploadService.port

    fun scanQrCode() {
        MyLog.i("Select QrCode")
        viewModelScope.launch(TestableDispatchers.IO) {
            navigation.openScanQrCode()?.let { qrCodeInfo ->
                MyLog.i("Start download service to download from: $qrCodeInfo")
                ContextCompat.startForegroundService(
                    context, DownloadService.createIntent(context, qrCodeInfo.url)
                )
                if (qrCodeInfo.version > Application.QR_CODE_VERSION) {
                    showToast(R.string.warning_newer_qrcode)
                } else {
                    showToast(R.string.info_download_start)
                }

            }
        }
    }

    fun receiveFromComputer() {
        MyLog.i("Select start web")
        viewModelScope.launch(TestableDispatchers.IO) {
            val uri = navigation.getSaveFolder()
            navigation.startWebUploadService(uri)
            withContext(TestableDispatchers.Main) {
                deviceIp.value = wifiApi.getIp()
                isSharing.value = true
            }
        }
    }

    fun stopWeb() {
        navigation.stopWebUploadService()
        isSharing.value = false
    }

    private fun showToast(@StringRes id: Int, duration: Int = Toast.LENGTH_LONG) =
        GlobalScope.launch(TestableDispatchers.Main) {
            Toast.makeText(context, id, duration).show()
        }


    fun isAbleToReceive() = isAbleToReceiveData

    fun isSharing() = isSharing
    fun deviceIp() = deviceIp
    fun devicePort() = devicePort

}
