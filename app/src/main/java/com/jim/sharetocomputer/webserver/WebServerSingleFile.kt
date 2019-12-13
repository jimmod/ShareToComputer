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

import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import com.jim.sharetocomputer.FileInfo
import com.jim.sharetocomputer.Message
import com.jim.sharetocomputer.WebServerService
import com.jim.sharetocomputer.ext.getFileName
import com.jim.sharetocomputer.logging.MyLog
import java.io.ByteArrayInputStream

class WebServerSingleFile(private val context: Context, port: Int) : WebServer(port) {

    private var uri: Uri? = null

    fun setUri(value: Uri) {
        uri = value
    }

    override fun serve(session: IHTTPSession?): Response {
        MyLog.i("Incoming http request from ${session?.remoteIpAddress}(${session?.remoteHostName}) to ${session?.uri}")
        notifyAccess()
        if (uri == null || session == null) {
            MyLog.w("Empty uri($uri) or session($session)")
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                ClipDescription.MIMETYPE_TEXT_PLAIN,
                Message.ERROR_CONTENT_NOT_SET
            )
        } else if (session.uri == "/info") {
            return infoResponse(1, listOf(FileInfo(context.getFileName(uri!!))))
        } else if (session.uri == "/kill") {
            context.stopService(WebServerService.createIntent(context, null))
            return newFixedLengthResponse(
                Response.Status.OK,
                "text/html",
                InputStreamNotifyWebServer(ByteArrayInputStream("".toByteArray()), this),
                -1
            )
        } else {
            val fis = context.contentResolver.openInputStream(uri!!)
            MyLog.d("*Response:$uri")
            return newFixedLengthResponse(
                Response.Status.OK,
                null,
                InputStreamNotifyWebServer(fis!!, this),
                -1
            ).apply {
                addHeader("Content-Disposition", "filename=\"${context.getFileName(uri!!)}\"")
            }
        }
    }
}
