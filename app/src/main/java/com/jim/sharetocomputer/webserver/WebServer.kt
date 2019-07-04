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
package com.jim.sharetocomputer.webserver

import com.google.gson.Gson
import com.jim.sharetocomputer.FileInfo
import com.jim.sharetocomputer.ShareInfo
import com.jim.sharetocomputer.logging.MyLog
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream

open class WebServer(port: Int): NanoHTTPD(port) {

    var lastAccessTime: Long = System.currentTimeMillis()
        protected set

    protected fun infoResponse(total: Int, files: List<FileInfo>): Response {
        val shareInfo = ShareInfo(total, files)
        val inputStream = ByteArrayInputStream(Gson().toJson(shareInfo).toByteArray())
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            InputStreamNotifyWebServer(inputStream, this),
            -1
        )
    }

    open fun notifyAccess() {
        lastAccessTime = System.currentTimeMillis()
    }

    override fun start() {
        MyLog.i("Starting WebServer")
        super.start()
    }
}