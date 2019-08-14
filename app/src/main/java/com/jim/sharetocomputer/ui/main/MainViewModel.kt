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

package com.jim.sharetocomputer.ui.main

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.jim.sharetocomputer.AllOpen
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.ShareRequest
import com.jim.sharetocomputer.WebServerService
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.ext.isOnWifi
import com.jim.sharetocomputer.logging.MyLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AllOpen
class MainViewModel(val context: Context) : ViewModel() {

    fun setRequest(request: ShareRequest?) {
        if (request != null) {
            MyLog.i("request found $request")
            if (!checkWifi()) return
            startWebService(request)
        } else {
            MyLog.i("no request")
        }
    }

    protected fun startWebService(request: ShareRequest) {
        val intent = WebServerService.createIntent(context, request)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MyLog.i("Starting web service foreground")
            context.startForegroundService(intent)
        } else {
            MyLog.i("Starting web service")
            context.startService(intent)
        }
    }

    protected fun checkWifi(): Boolean {
        if (context.isOnWifi()) return true
        MyLog.i("No Wi-Fi network detected")
        showToast(R.string.error_wifi_required)
        return false
    }

    protected fun showToast(@StringRes id: Int) {
        GlobalScope.launch(TestableDispatchers.Main) {
            Toast.makeText(context, id, Toast.LENGTH_LONG).show()
        }
    }

}