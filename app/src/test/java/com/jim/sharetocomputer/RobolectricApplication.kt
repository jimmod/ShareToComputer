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

import android.app.Application
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.ConsolePrinter
import com.jim.sharetocomputer.coroutines.DirectDispatcher
import com.jim.sharetocomputer.coroutines.TestableDispatchers


class RobolectricApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        setupLogging()

        setupCoroutinesDispatchers()

        WebServerService.isRunning.value = false

        setupWifiConnection()
    }

    private fun setupLogging() {
        val config = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .build()

        XLog.init(
            config,
            ConsolePrinter()
        )
    }

    private fun setupCoroutinesDispatchers() {
        TestableDispatchers.setMain(DirectDispatcher())
        TestableDispatchers.setDefault(DirectDispatcher())
        TestableDispatchers.setIo(DirectDispatcher())
        TestableDispatchers.setUnconfined(DirectDispatcher())
    }
}