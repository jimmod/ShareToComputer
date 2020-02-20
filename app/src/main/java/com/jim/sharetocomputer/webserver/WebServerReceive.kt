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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.jim.sharetocomputer.Application
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.ext.getAppName
import com.jim.sharetocomputer.logging.MyLog
import fi.iki.elonen.NanoHTTPD.Response.IStatus
import fi.iki.elonen.NanoHTTPD.Response.Status
import java.io.*

class WebServerReceive(val context: Context, port: Int) : WebServer(port) {

    override fun serve(session: IHTTPSession?): Response {
        MyLog.i("${session?.uri}")

        if (session?.uri == "/upload") {
            if (session.method != Method.POST) return newFixedLengthResponse(
                Status.METHOD_NOT_ALLOWED,
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
                    Status.BAD_REQUEST,
                    "text/html",
                    "No file uploaded"
                )
            }
            //TODO check Android 10 compatibility (getExternalStoragePublicDirectory is deprecated)
            @Suppress("DEPRECATION") val dst =
                File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), filename[0])
            if (dst.exists()) {
                return generateHtmlFileResponse(
                    "web/upload_failed_file_exist.html",
                    Status.BAD_REQUEST
                ) {
                    it.replace("[[title]]", context.getAppName())
                        .replace(
                            "[[error_failed_web_upload]]",
                            context.getString(R.string.error_failed_web_upload)
                        )
                        .replace(
                            "[[upload_another_file]]",
                            context.getString(R.string.upload_another_file)
                        )
                        .toByteArray()
                }
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
            val fileUri = FileProvider.getUriForFile(
                context, "com.jim.sharetocomputer.provider", File(dst.absolutePath)
            )
            createSuccessNotification(fileUri)
            return generateHtmlFileResponse("web/upload_success.html", Status.OK) {
                it.replace("[[title]]", context.getAppName())
                    .replace(
                        "[[success_web_upload]]",
                        context.getString(R.string.success_web_upload)
                    )
                    .replace(
                        "[[upload_another_file]]",
                        context.getString(R.string.upload_another_file)
                    )
                    .toByteArray()
            }
        } else if (session?.uri == "/") {
            return generateHtmlFileResponse("web/receive.html", Status.OK) {
                it.replace("[[title]]", context.getAppName()).toByteArray()
            }

        }
        return super.serve(session)
    }

    private fun createSuccessNotification(uri: Uri) {
        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Application.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
        val openFileIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.packageManager.queryIntentActivities(
            openFileIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        ).forEach {
            context.grantUriPermission(
                it.activityInfo.packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val openFilePendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(openFileIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val builder = NotificationCompat.Builder(context, Application.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.info_success_web_upload))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openFilePendingIntent)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun generateHtmlFileResponse(
        filename: String,
        status: IStatus,
        contentModifier: (String) -> ByteArray
    ): Response {
        var content: ByteArray? = null
        context.assets.open(filename).use {
            content = contentModifier(String(it.readBytes()))
        }
        return newFixedLengthResponse(
            status,
            "text/html",
            InputStreamNotifyWebServer(ByteArrayInputStream(content), this),
            -1
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 5001
    }

}
