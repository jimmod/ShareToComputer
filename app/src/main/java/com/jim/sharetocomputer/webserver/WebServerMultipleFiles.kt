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
import com.jim.sharetocomputer.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class WebServerMultipleFiles(private val context: Context, port: Int) : WebServer(port) {

    private var uris: Collection<Uri>? = null

    fun setUris(value: Collection<Uri>) {
        uris = value
    }

    override fun serve(session: IHTTPSession?): Response {
        Timber.d("Incoming http request")
        if (uris == null) return newFixedLengthResponse(
            Response.Status.NOT_FOUND,
            ClipDescription.MIMETYPE_TEXT_PLAIN,
            Message.ERROR_CONTENT_NOT_SET
        )
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream(outputStream)

        return newFixedLengthResponse(
            Response.Status.OK,
            "application/zip",
            inputStream,
            -1
        ).also {
            GlobalScope.launch {
                writeZip(outputStream)
            }
        }
    }

    private suspend fun writeZip(outputStream: OutputStream) = withContext(Dispatchers.Default) {
        Timber.d("Streaming zip file")
        try {
            ZipOutputStream(outputStream).use { zip ->
                uris!!.forEach { uri ->
                    val fis = context.contentResolver.openInputStream(uri)
                    zip.putNextEntry(ZipEntry(context.getFileName(uri)))
                    fis?.readBytes()?.let {
                        zip.write(it)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            outputStream.close()
        }
    }
}