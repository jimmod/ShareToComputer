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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.jim.sharetocomputer.AllOpen
import com.jim.sharetocomputer.QrCodeInfo
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.WebUploadService
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.gateway.ActivityHelper
import com.jim.sharetocomputer.logging.MyLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AllOpen
class ReceiveNavigation(val fragment: Fragment, val activityHelper: ActivityHelper) :
    KoinComponent {

    suspend fun openScanQrCode() = suspendCoroutine<QrCodeInfo?> { cont ->
        activityHelper.startQrCodeScan(fragment.activity!!)?.let { result ->
            val resultQrCode: IntentResult? = IntentIntegrator.parseActivityResult(
                IntentIntegrator.REQUEST_CODE,
                result.resultCode,
                result.resultData
            )
            try {
                cont.resume(Gson().fromJson(resultQrCode!!.contents, QrCodeInfo::class.java))
                return@suspendCoroutine
            } catch (e: JsonSyntaxException) {
                MyLog.w("Error on parsing QR Code result", e)
                GlobalScope.launch(TestableDispatchers.Main) {
                    Toast.makeText(
                        fragment.activity!!,
                        R.string.warning_unknown_qrcode,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        cont.resume(null)
    }

    fun startWebUploadService(uri: Uri) {
        fragment.context?.run {
            val intent = Intent(this, WebUploadService::class.java)
            intent.putExtra(WebUploadService.EXTRA_KEY_URI, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent)
            } else {
                this.startService(intent)
            }
        }
    }

    fun stopWebUploadService() {
        fragment.context?.run {
            stopService(
                Intent(this, WebUploadService::class.java)
            )
        }
    }

    fun getSaveFolder(): Uri {
        val result = activityHelper.startActivityForResult(
            fragment.activity!!,
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
        )

        if (result?.resultCode == Activity.RESULT_OK) {
            return result.resultData?.data!!
        }
        throw IllegalAccessException()
    }

}
