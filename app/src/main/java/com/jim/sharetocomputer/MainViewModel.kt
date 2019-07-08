package com.jim.sharetocomputer

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.ext.convertDpToPx
import com.jim.sharetocomputer.ext.getIp
import com.jim.sharetocomputer.ext.isOnWifi
import com.jim.sharetocomputer.ext.startActivityForResult
import com.jim.sharetocomputer.logging.MyLog
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainViewModel(private val port: Int) : ViewModel() {

    private var request: ShareRequest? = null
    val deviceIp = MutableLiveData<String>().apply { value = "unknown" }
    val devicePort = MutableLiveData<Int>().apply { value = port }
    var qrCode = MutableLiveData<Drawable>()
    private var qrCodeBitmap: Bitmap? = null
    lateinit var context: FragmentActivity

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
            context.startActivityForResult(intent)?.let { result ->
                handleSelectFileResult(result)
            }
        }
    }

    fun selectMedia() {
        MyLog.i("Select Media")
        if (!checkWifi()) return
        GlobalScope.launch(TestableDispatchers.Default) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            context.startActivityForResult(intent)?.let { result ->
                handleSelectFileResult(result)
            }
        }
    }

    private fun handleSelectFileResult(result: Instrumentation.ActivityResult) {
        MyLog.i("*Result: ${result.resultCode}|${result.resultData?.extras?.keySet()}")
        if (result.resultCode == Activity.RESULT_OK) {
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
            deviceIp.value = context.getIp()
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
                context.getString(R.string.qrcode_url, context.getIp(), port.toString())
            )
        )
        return barcodeEncoder.encodeBitmap(
            barcodeContent,
            BarcodeFormat.QR_CODE,
            context.convertDpToPx(200F).toInt(), context.convertDpToPx(200F).toInt()
        )
    }


    fun setRequest(request: ShareRequest?) {
        this.request = request
        if (request != null) {
            MyLog.i("request found $request")
            startWebService(request)
        } else {
            MyLog.i("no request")
        }
    }

    fun stopShare() {
        stopWebService()
    }

    private fun startWebService(request: ShareRequest) {
        val intent = WebServerService.createIntent(context, request)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MyLog.i("Starting web service foreground")
            context.startForegroundService(intent)
        } else {
            MyLog.i("Starting web service")
            context.startService(intent)
        }
    }

    private fun stopWebService() {
        val intent = WebServerService.createIntent(context, null)
        context.stopService(intent)
    }

    private fun checkWifi(): Boolean {
        if (context.isOnWifi()) return true
        showToast(R.string.error_wifi_required)
        return false
    }

    private fun showToast(@StringRes id: Int) {
        GlobalScope.launch(TestableDispatchers.Main) {
            Toast.makeText(context, id, Toast.LENGTH_LONG).show()
        }
    }

    fun isSharing(): MutableLiveData<Boolean> {
        return WebServerService.isRunning
    }
}