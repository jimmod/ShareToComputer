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

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.jim.sharetocomputer.databinding.FragmentMainBinding
import com.jim.sharetocomputer.ext.convertDpToPx
import com.jim.sharetocomputer.ext.getIp
import com.jim.sharetocomputer.logging.MyLog
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named


class MainFragment : Fragment() {

    private val port by inject<Int>(named("PORT"))
    private val mainViewModel: MainViewModel by viewModel()
    private var qrCodeBitmap: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MyLog.i("onCreate")
        qrCodeBitmap = generateQrCode()
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = mainViewModel
        mainViewModel.qrcode.value = BitmapDrawable(activity!!.resources, qrCodeBitmap)
        mainViewModel.context = activity!!

        val request = arguments?.get(ARGS_REQUEST) as ShareRequest?
        mainViewModel.setRequest(request)
        return binding.root
    }

    private fun generateQrCode(): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        val barcodeContent = Gson().toJson(
            QrCodeInfo(
                Application.QR_CODE_VERSION,
                context!!.getString(R.string.qrcode_url, context!!.getIp(), port.toString())
            )
        )
        return barcodeEncoder.encodeBitmap(
            barcodeContent,
            BarcodeFormat.QR_CODE,
            context!!.convertDpToPx(200F).toInt(), context!!.convertDpToPx(200F).toInt()
        )
    }

    override fun onDestroyView() {
        MyLog.i("onDestroy")
        qrCodeBitmap?.recycle()
        super.onDestroyView()
    }

    companion object {
        private const val ARGS_REQUEST = "request"

        fun createBundle(request: ShareRequest): Bundle {
            return Bundle().apply {
                putParcelable(ARGS_REQUEST, request)
            }
        }
    }

}