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

package com.jim.sharetocomputer.ui.send

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.jim.sharetocomputer.*
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.ext.convertDpToPx
import com.jim.sharetocomputer.gateway.ActivityHelper
import com.jim.sharetocomputer.gateway.WifiApi
import com.jim.sharetocomputer.logging.MyLog
import com.jim.sharetocomputer.ui.main.MainViewModel
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AllOpen
class SendViewModel(context: Context, val wifiApi: WifiApi, val activityHelper: ActivityHelper) :
    MainViewModel(context) {

    private val deviceIp = MutableLiveData<String>().apply { value = "unknown" }
    private val isAbleToShareData = MediatorLiveData<Boolean>().apply {
        addSource(WebUploadService.isRunning) {
            this.value = !it
        }
    }
    private val devicePort = WebServerService.port
    private var qrCode = MutableLiveData<Drawable>()
    private var qrCodeBitmap: Bitmap? = null
    lateinit var activity: FragmentActivity

    init {
        updateWebServerUi()
    }

    fun selectFile() {
        MyLog.i("Select File")
        if (!checkWifi()) return
        GlobalScope.launch(TestableDispatchers.Default) {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            activityHelper.startActivityForResult(activity, intent)?.let { result ->
                handleSelectFileResult(result)
            }
        }
    }

    fun selectMedia() {
        MyLog.i("Select Media")
        if (!checkWifi()) return
        GlobalScope.launch(TestableDispatchers.Default) {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            activityHelper.startActivityForResult(activity, intent)?.let { result ->
                handleSelectFileResult(result)
            }
        }
    }

    fun isAbleToShare(): LiveData<Boolean> = isAbleToShareData

    private fun handleSelectFileResult(result: Instrumentation.ActivityResult) {
        MyLog.i("*Result: ${result.resultCode}|${result.resultData?.extras?.keySet()}")
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            updateWebServerUi()
            result.resultData.data?.run {
                startWebService(ShareRequest.ShareRequestSingleFile(this))
            }
            result.resultData.clipData?.run {
                if (this.itemCount == 1) {
                    startWebService(ShareRequest.ShareRequestSingleFile(this.getItemAt(0).uri))
                } else {
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until this.itemCount) {
                        uris.add(this.getItemAt(i).uri)
                    }
                    startWebService(ShareRequest.ShareRequestMultipleFile(uris))
                }
            }
        }
    }

    private fun updateWebServerUi() {
        GlobalScope.launch(TestableDispatchers.Main) {
            deviceIp.value = wifiApi.getIp()
            qrCodeBitmap?.recycle()
            qrCodeBitmap = generateQrCode()
            qrCode.value = BitmapDrawable(context.resources, qrCodeBitmap)
        }
    }

    private fun generateQrCode(): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        val barcodeContent = Gson().toJson(
            QrCodeInfo(
                Application.QR_CODE_VERSION,
                context.getString(
                    R.string.qrcode_url,
                    wifiApi.getIp(),
                    devicePort.value.toString()
                )
            )
        )
        return barcodeEncoder.encodeBitmap(
            barcodeContent,
            BarcodeFormat.QR_CODE,
            context.convertDpToPx(200F).toInt(), context.convertDpToPx(200F).toInt()
        )
    }

    fun stopShare() {
        stopWebService()
    }


    private fun stopWebService() {
        val intent = WebServerService.createIntent(context, null)
        context.stopService(intent)
    }

    fun isSharing(): MutableLiveData<Boolean> {
        return WebServerService.isRunning
    }

    fun deviceIp() = deviceIp
    fun devicePort() = devicePort
    fun qrCode() = qrCode
}
