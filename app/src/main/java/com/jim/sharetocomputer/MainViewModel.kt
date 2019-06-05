package com.jim.sharetocomputer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(private val context: FragmentActivity, private val port: Int) : ViewModel() {

    private var request: ShareRequest? = null
    val ip = MutableLiveData<String>().apply { value = "lalala" }
    private val instructionVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    private val downloadVisibility = MutableLiveData<Int>().apply { value = View.GONE }

    private fun displayInstruction(visible: Boolean = true) {
        GlobalScope.launch(TestableDispatchers.Main) {
            if (visible && instructionVisibility.value!=View.VISIBLE) {
                instructionVisibility.value = View.VISIBLE
                downloadVisibility.value = View.GONE
            } else if (!visible && downloadVisibility.value!=View.VISIBLE) {
                instructionVisibility.value = View.GONE
                downloadVisibility.value = View.VISIBLE
            }
        }
    }

    fun selectFile() {
        Timber.d("select file")
        GlobalScope.launch(TestableDispatchers.Default) {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            context.startActivityForResult(intent)?.let {result ->
                Timber.d("Result: ${result.resultCode}|${result.resultData.data}")
                if (result.resultCode == Activity.RESULT_OK) {
                    result.resultData.data?.run {
                        startWebService(ShareRequest.ShareRequestSingleFile(this))
                    }
                    result.resultData.clipData?.run {
                        val uris = mutableListOf<Uri>()
                        for (i in 0 until this.itemCount) {
                            uris.add(this.getItemAt(i).uri)
                        }
                        startWebService(ShareRequest.ShareRequestMultipleFile(uris))
                    }
                    displayInstruction(false)
                }
            }
        }
    }

    fun setRequest(request: ShareRequest?) {
        this.request = request
        if (request != null) {
            Timber.d("request found $request")
            startWebService(request)
            displayInstruction(false)
        } else {
            Timber.d("no request")
            displayInstruction()
        }
    }

    fun stopShare() {
        stopWebService()
        displayInstruction()
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

    fun deviceIp(): String {
        val wifiManager =
            context.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager? ?: return "0.0.0.0"
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }

    fun devicePort(): Int {
        return port
    }

    fun downloadVisibility(): MutableLiveData<Int> {
        if (WebServerService.isRunning) {
            displayInstruction(false)
        } else {
            displayInstruction()
        }
        return downloadVisibility
    }

    fun instructionVisibility(): MutableLiveData<Int> {
        if (WebServerService.isRunning) {
            displayInstruction(false)
        } else {
            displayInstruction()
        }
        return instructionVisibility
    }
}