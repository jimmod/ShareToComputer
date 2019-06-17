package com.jim.sharetocomputer

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.ext.getIp
import com.jim.sharetocomputer.ext.startActivityForResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class MainViewModel(private val context: FragmentActivity, private val port: Int) : ViewModel() {

    private var request: ShareRequest? = null
    val ip = MutableLiveData<String>().apply { value = "unknown" }
    var qrcode = MutableLiveData<Drawable>()

    fun selectFile() {
        Timber.d("select file")
        GlobalScope.launch(TestableDispatchers.Default) {
            val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            context.startActivityForResult(intent)?.let { result ->
                handleSelectFileResult(result)
            }
        }
    }

    fun selectMedia() {
        Timber.d("select media")
        GlobalScope.launch(TestableDispatchers.Default) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            context.startActivityForResult(intent)?.let {result ->
                handleSelectFileResult(result)
            }
        }
    }

    fun scanQrCode() {
        Timber.d("scan qr code")
    }

    private fun handleSelectFileResult(result: Instrumentation.ActivityResult) {
        Timber.d("Result: ${result.resultCode}|${result.resultData?.data}")
        if (result.resultCode == Activity.RESULT_OK) {
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

    fun deviceIp(): String = context.getIp()

    fun setRequest(request: ShareRequest?) {
        this.request = request
        if (request != null) {
            Timber.d("request found $request")
            startWebService(request)
        } else {
            Timber.d("no request")
        }
    }

    fun stopShare() {
        stopWebService()
    }

    private fun startWebService(request: ShareRequest) {
        val intent = WebServerService.createIntent(context, request)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("Starting web service foreground")
            context.startForegroundService(intent)
        } else {
            Timber.d("Starting web service")
            context.startService(intent)
        }
    }

    private fun stopWebService() {
        val intent = WebServerService.createIntent(context, null)
        context.stopService(intent)
    }

    fun devicePort(): Int {
        return port
    }

    fun isSharing(): MutableLiveData<Boolean> {
        return WebServerService.isRunning
    }
}