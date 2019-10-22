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

import android.app.Instrumentation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.ext.startQrCodeScan
import com.jim.sharetocomputer.logging.MyLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class QrCodeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MyLog.i("onCreate")
        GlobalScope.launch(TestableDispatchers.Default) {
            activity?.startQrCodeScan()?.let { result ->
                handleQrCodeResult(result)

                GlobalScope.launch(TestableDispatchers.Main) {
                    findNavController().popBackStack()
                }

            }
        }
        return null
    }

    override fun onDestroy() {
        MyLog.i("onDestroy")
        super.onDestroy()
    }

    private fun handleQrCodeResult(result: Instrumentation.ActivityResult) {
        val resultQrCode: IntentResult? = IntentIntegrator.parseActivityResult(
            IntentIntegrator.REQUEST_CODE,
            result.resultCode,
            result.resultData
        )
        try {
            Gson().fromJson(resultQrCode!!.contents, QrCodeInfo::class.java)?.let { qrCodeInfo ->
                MyLog.i("Start download service to download from: $qrCodeInfo")
                ContextCompat.startForegroundService(
                    activity!!,
                    DownloadService.createIntent(activity!!, qrCodeInfo.url)
                )
                if (qrCodeInfo.version > Application.QR_CODE_VERSION) {
                    showToast(R.string.warning_newer_qrcode)
                } else {
                    showToast(R.string.info_download_start)
                }

            }
        } catch (e: JsonSyntaxException) {
            MyLog.w("Error on parsing QR Code result", e)
            showToast(R.string.warning_unknown_qrcode)
        }
    }

    private fun showToast(@StringRes id: Int, duration: Int = Toast.LENGTH_LONG) =
        GlobalScope.launch(TestableDispatchers.Main) {
            Toast.makeText(activity, id, duration).show()
        }

}
