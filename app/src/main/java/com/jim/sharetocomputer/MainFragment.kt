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

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.jim.sharetocomputer.databinding.FragmentMainBinding
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import timber.log.Timber


class MainFragment : Fragment() {

    val port by inject<Int>(named("PORT"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        arguments?.keySet()?.forEach {
            Timber.d("$it: ${arguments?.get(it)}")
        }

        val request = arguments?.get(ARGS_REQUEST)
        if (request!=null) {
            val intent = WebServerService.createIntent(activity!!, request as ShareRequest)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity?.startForegroundService(intent)
            } else {
                activity?.startService(intent)
            }
            binding.instruction = getString(R.string.download_instruction, "http://${getIp()}:$port")
        } else {
            binding.instruction = getString(R.string.default_instruction)
        }
        return binding.root
    }

    private fun getIp(): String {
        val wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager? ?: return "0.0.0.0"
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
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