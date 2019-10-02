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

package com.jim.sharetocomputer.webserver

import android.content.Context
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.ext.getAppName
import com.jim.sharetocomputer.logging.MyLog
import java.io.*

class WebServerReceive(val context: Context, port: Int) : WebServer(port) {

    override fun serve(session: IHTTPSession?): Response {
        MyLog.i("${session?.uri}")

        if (session?.uri == "/upload") {
            if (session.method != Method.POST) return newFixedLengthResponse(
                Response.Status.METHOD_NOT_ALLOWED,
                "text/html",
                "HTTP POST ONLY"
            )
            val params = session.parameters
            val files = HashMap<String, String>()
            session.parseBody(files)

            val filename = params["fileToUpload"]
            val tmpFilePath = files["fileToUpload"]
            if (null == filename || null == tmpFilePath) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    "text/html",
                    "No file uploaded"
                )
            }
            //TODO check Android 10 compatibility (getExternalStoragePublicDirectory is deprecated)
            @Suppress("DEPRECATION") val dst =
                File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), filename[0])
            if (dst.exists()) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    "text/html",
                    "File exist in phone download folder"
                )
            }
            val src = File(tmpFilePath)
            try {
                val ins = FileInputStream(src)
                val out = FileOutputStream(dst)
                val buf = ByteArray(65536)
                var len: Int
                while (ins.read(buf).also { len = it } >= 0) {
                    out.write(buf, 0, len)
                }
                ins.close()
                out.close()
            } catch (ioe: IOException) {
            }
            return newFixedLengthResponse(context.getString(R.string.success_web_upload))
        } else if (session?.uri == "/") {
            var content: ByteArray? = null
            context.assets.open("web/receive.html").use {
                content = String(it.readBytes())
                    .replace("[[title]]", context.getAppName())
                    .toByteArray()
            }
            return newFixedLengthResponse(
                Response.Status.OK,
                "text/html",
                InputStreamNotifyWebServer(ByteArrayInputStream(content), this),
                -1
            )
        }
        return super.serve(session)
    }

}