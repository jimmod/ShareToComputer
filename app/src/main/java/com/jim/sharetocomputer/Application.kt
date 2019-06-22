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

import com.jim.sharetocomputer.logging.KoinLogger
import com.jim.sharetocomputer.logging.MyLog
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin


open class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        MyLog.setupLogging(this)
        MyLog.i("Application is starting")
        MyLog.i("*QR Code version: $QR_CODE_VERSION")

        startKoin {
            KoinApplication.logger = KoinLogger()
            androidContext(this@Application)
            modules(applicationModule)
        }
    }

    companion object {
        const val QR_CODE_VERSION = 1
        const val CHANNEL_ID = "DEFAULT_CHANNEL"
    }

}
