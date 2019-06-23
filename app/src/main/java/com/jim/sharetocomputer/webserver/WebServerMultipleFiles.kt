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
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.ext.appName
import com.jim.sharetocomputer.ext.getFileName
import com.jim.sharetocomputer.logging.MyLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
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
        MyLog.i("Incoming http request from ${session?.remoteIpAddress}(${session?.remoteHostName}) to ${session?.uri}")
        try {
            return if (uris == null || session == null) {
                throw IllegalArgumentException()
            } else if (session.uri == "/info") {
                infoResponse(
                    uris?.size ?: 0,
                    uris?.map { FileInfo(context.getFileName(it)) } ?: emptyList()
                )
            } else if (session.uri == "/zip") {
                zipResponse()
            } else if (session.uri.matches("/[0-9]+".toRegex())) {
                val index = session.uri.split("/")[1].toInt()
                if (index >= uris!!.size) throw IllegalArgumentException()
                val uri = uris!!.elementAt(index)
                MyLog.d("*Response uris[$index]:$uri")
                contentUriResponse(uri)
            } else if (session.uri == "/") {
                mainWebResponse()
            } else {
                throw IllegalArgumentException()
            }
        } catch (e: IllegalArgumentException) {
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                ClipDescription.MIMETYPE_TEXT_PLAIN,
                Message.ERROR_CONTENT_NOT_SET
            )
        }
    }

    private fun mainWebResponse(): Response {
        var content: ByteArray? = null
        context.assets.open("web/main.html").use {
            content = String(it.readBytes())
                .replace("[[tbody]]", generateTbody())
                .toByteArray()
        }
        return newFixedLengthResponse(
            Response.Status.OK,
            "text/html",
            ByteArrayInputStream(content),
            -1
        )
    }

    private fun zipResponse(): Response {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream(outputStream)

        return newFixedLengthResponse(
            Response.Status.OK,
            "application/zip",
            inputStream,
            -1
        ).apply {
            addHeader("Content-Disposition", "filename=\"${context.appName()}.zip\"")
        }.also {
            GlobalScope.launch {
                writeZip(outputStream)
            }
        }
    }

    private fun contentUriResponse(uri: Uri): Response {
        val fis = context.contentResolver.openInputStream(uri)
        return newFixedLengthResponse(
            Response.Status.OK,
            null,
            fis,
            -1
        ).apply {
            addHeader("Content-Disposition", "filename=\"${context.getFileName(uri)}\"")
        }
    }

    private fun generateTbody(): String {
        val tbody = StringBuilder()
        uris?.forEachIndexed { index, uri ->
            tbody.append(
                tableBody(index, context.getFileName(uri))
            )
        }
        return tbody.toString()
    }

    private suspend fun writeZip(outputStream: OutputStream) = withContext(Dispatchers.Default) {
        try {
            ZipOutputStream(outputStream).use { zip ->
                uris!!.forEach { uri ->
                    val fis = context.contentResolver.openInputStream(uri)
                    zip.putNextEntry(ZipEntry(context.getFileName(uri)))
                    fis?.readBytes()?.let {
                        zip.write(it)
                    }
                }
                MyLog.i("Streaming zip file done")
            }
        } catch (e: Exception) {
            MyLog.e("Error on streaming zip", e)
        } finally {
            outputStream.close()
        }
    }

    private fun tableBody(index: Int, filename: String) = """
        <tr>
        <td>${index + 1}</td>
        <td>$filename</td>
        <td><a href="/$index">${context.getString(R.string.download)}</a></td>
        </tr>
    """.trimIndent()

}