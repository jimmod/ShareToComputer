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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jim.sharetocomputer.webserver.WebServer
import com.jim.sharetocomputer.webserver.WebServerMultipleFiles
import com.jim.sharetocomputer.webserver.WebServerSingleFile
import com.jim.sharetocomputer.webserver.WebServerText
import org.koin.android.ext.android.get
import timber.log.Timber

class WebServerService : Service() {

    private var webServer: WebServer? = null
    private var stopTime: Long = Long.MAX_VALUE

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        startForeground(NOTIFICATION_ID, createNotification())
        intent?.getParcelableExtra<ShareRequest>(EXTRA_REQUEST)?.let { request ->
            webServer?.stop()
            webServer = when (request) {
                is ShareRequest.ShareRequestText -> get<WebServerText>().apply { setText(request.text) }
                is ShareRequest.ShareRequestSingleFile -> get<WebServerSingleFile>().apply { setUri(request.uri) }
                is ShareRequest.ShareRequestMultipleFile -> get<WebServerMultipleFiles>().apply { setUris(request.uris) }
            }
            stopTime = System.currentTimeMillis() + TIME_AUTO_STOP
            Timber.d("Starting WebServer")
            StopperThread().start()
            webServer!!.start()

            return START_STICKY
        }

        stopSelf()
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_server_title))
            .setContentText(getString(R.string.notification_server_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        return builder.build()
    }


    inner class StopperThread: Thread() {
        override fun run() {
            while (true) {
                if (System.currentTimeMillis() >= stopTime) {
                    stopSelf()
                    break
                }
                sleep(stopTime-System.currentTimeMillis())
            }
        }
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        webServer?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val EXTRA_REQUEST = "request"
        private const val TIME_AUTO_STOP = 5 * 60 * 1000
        private const val NOTIFICATION_ID = 1945
        private const val CHANNEL_ID = "DEFAULT_CHANNEL"

        fun createIntent(context: Context, request: ShareRequest): Intent {
            return Intent(context, WebServerService::class.java).apply {
                putExtra(EXTRA_REQUEST, request)
            }
        }
    }

}
