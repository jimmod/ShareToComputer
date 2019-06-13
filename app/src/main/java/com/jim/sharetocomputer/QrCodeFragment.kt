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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.ext.startBarcodeScan
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class QrCodeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        GlobalScope.launch(TestableDispatchers.Default) {
            activity?.startBarcodeScan()?.let { result ->
                val resultBarcode: IntentResult? = IntentIntegrator.parseActivityResult(
                    IntentIntegrator.REQUEST_CODE,
                    result.resultCode,
                    result.resultData
                )
                val qrCodeInfo = Gson().fromJson(resultBarcode!!.contents, QrCodeInfo::class.java)
                Timber.d("Downloading from: $qrCodeInfo")
                ContextCompat.startForegroundService(
                    activity!!,
                    DownloadService.createIntent(activity!!, qrCodeInfo.url)
                )

                GlobalScope.launch(TestableDispatchers.Main) {
                    if (qrCodeInfo.version > Application.QR_CODE_VERSION) {
                        Toast.makeText(activity, R.string.warning_newer_qrcode, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, R.string.info_download_start, Toast.LENGTH_LONG).show()
                    }
                    findNavController().popBackStack()
                }

            }
        }
        return null
    }

}