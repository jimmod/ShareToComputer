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

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStreamReader
import java.net.URL

class DownloadService : Service() {

    private val ids = mutableListOf<Long>()
    private val onComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            Timber.d("download completed: $id")
            ids.remove(id)
            if (ids.isEmpty()) {
                Timber.d("all download completed")
                Toast.makeText(this@DownloadService, R.string.info_download_completed, Toast.LENGTH_LONG).show()
                stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        GlobalScope.launch {
            onHandleIntent(intent)
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        unregisterReceiver(onComplete)
        super.onDestroy()
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
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(this, Application.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.downloading))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        return builder.build()
    }

    private suspend fun onHandleIntent(intent: Intent?) = withContext(TestableDispatchers.Default) {
        Timber.d("onHandleIntent")
        val url = intent!!.getStringExtra(EXTRA_URL)
        val shareInfo = downloadInfo(url)
        val requests = getDownloadRequests(url, shareInfo)

        val downloadManager = getSystemService(DownloadManager::class.java)
        requests.forEachIndexed { index, request ->
            val id = downloadManager.enqueue(request)
            ids.add(id)
            Timber.d("*enqueue[$index]: $id")
        }
    }

    private fun getDownloadRequests(url: String, shareInfo: ShareInfo): List<DownloadManager.Request> {
        return shareInfo.files.mapIndexed { index, fileInfo ->
            val uri = Uri.parse("$url/$index")
            return@mapIndexed DownloadManager.Request(uri).apply {
                this.setTitle(getString(R.string.app_name))
                this.setDescription(getString(R.string.downloading))
                this.setMimeType("*/*")
                this.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                this.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileInfo.filename)
            }
        }
    }

    private fun downloadInfo(client: String): ShareInfo {
        val url = URL("$client/info")
        val inputStream = InputStreamReader(url.openStream())

        return Gson().fromJson(inputStream, ShareInfo::class.java)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {

        private const val EXTRA_URL = "url"
        private const val NOTIFICATION_ID = 4519

        fun createIntent(context: Context, url: String): Intent {
            return Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }

    }

}