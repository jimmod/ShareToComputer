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
@file:Suppress("DEPRECATION")

package com.jim.sharetocomputer

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.jim.sharetocomputer.ext.getIp
import com.jim.sharetocomputer.logging.MyLog
import com.jim.sharetocomputer.webserver.WebServer
import com.jim.sharetocomputer.webserver.WebServerMultipleFiles
import com.jim.sharetocomputer.webserver.WebServerSingleFile
import com.jim.sharetocomputer.webserver.WebServerText
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import java.io.IOException
import java.net.ServerSocket

class WebServerService : Service() {

    private var webServer: WebServer? = null
    private var stopper: StopperThread? = null
    private val wifiListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val info = intent?.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
            if (info == null || !info.isConnected) {
                MyLog.i("Stopping service because Wi-Fi disconnected")
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning.value = true
        val filter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        registerReceiver(wifiListener, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MyLog.i("onStartCommand")
        startForeground(NOTIFICATION_ID, createNotification())
        intent?.getParcelableExtra<ShareRequest>(EXTRA_REQUEST)?.let { request ->
            webServer?.stop()
            stopper?.cancel()
            val port = findFreePort()
            webServer = when (request) {
                is ShareRequest.ShareRequestText -> get<WebServerText>(parameters = {
                    parametersOf(
                        port
                    )
                }).apply {
                    setText(
                        request.text
                    )
                }
                is ShareRequest.ShareRequestSingleFile -> get<WebServerSingleFile>(parameters = {
                    parametersOf(
                        port
                    )
                }).apply {
                    setUri(
                        request.uri
                    )
                }
                is ShareRequest.ShareRequestMultipleFile -> get<WebServerMultipleFiles>(parameters = {
                    parametersOf(
                        port
                    )
                }).apply {
                    setUris(
                        request.uris
                    )
                }
            }
            stopper = StopperThread(this, webServer!!).also { it.start() }
            WebServerService.port.value = port
            webServer!!.start()
            startForeground(NOTIFICATION_ID, createNotification())

            return START_STICKY
        }

        stopSelf()
        return START_NOT_STICKY
    }

    private fun findFreePort(): Int {
        for (port in 8080..8100) {
            var socket: ServerSocket? = null
            try {
                socket = ServerSocket(port)
            } catch (ex: IOException) {
                continue
            } finally {
                try {
                    socket?.close()
                } catch (ex: IOException) {
                }
            }
            MyLog.i("free port: $port")
            return port
        }
        MyLog.e("no free port found")
        throw IOException("no free port found")
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Application.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.createNotificationChannel(channel)
        }
        val stopIntent = ActionActivity.stopShareIntent(this)
        val stopPendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(stopIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val builder = NotificationCompat.Builder(this, Application.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(
                getString(
                    R.string.notification_server_text,
                    this.getIp(),
                    WebServerService.port.value.toString()
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                R.mipmap.ic_launcher, getString(R.string.stop_share),
                stopPendingIntent
            )
        return builder.build()
    }

    override fun onDestroy() {
        MyLog.i("onDestroy")
        unregisterReceiver(wifiListener)
        webServer?.stop()
        stopper?.cancel()
        isRunning.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val EXTRA_REQUEST = "request"
        private const val NOTIFICATION_ID = 1945

        var isRunning = MutableLiveData<Boolean>().apply { value = false }
        var port = MutableLiveData<Int>().apply { value = 8080 }

        fun createIntent(context: Context, request: ShareRequest?): Intent {
            return Intent(context, WebServerService::class.java).apply {
                request?.let { putExtra(EXTRA_REQUEST, it) }
            }
        }
    }

}

class StopperThread(
    private val service: Service,
    private val webServer: WebServer,
    private val autoStopTime: Int = TIME_AUTO_STOP
) : Thread() {

    private var isStopped = false

    override fun run() {
        while (!isStopped) {
            val autoStopTime = webServer.lastAccessTime + autoStopTime
            if (System.currentTimeMillis() >= autoStopTime) {
                MyLog.i("Auto stop service after ${autoStopTime}ms")
                service.stopSelf()
                break
            }
            sleep(autoStopTime - System.currentTimeMillis())
        }
    }

    fun cancel() {
        isStopped = true
    }

    companion object {
        private const val TIME_AUTO_STOP = 5 * 60 * 1000
    }
}
