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
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.downloader.StcApi
import com.jim.sharetocomputer.logging.MyLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URL


class DownloadService : Service(), KoinComponent {

    private val queuedIds = mutableSetOf<Long>()
    private val completeIds = mutableSetOf<Long>()
    private val onComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1
            MyLog.i("A download completed: $id")
            if (id == -1L) return
            completeIds.add(id)
            if (queuedIds.size == completeIds.size) {
                MyLog.i("All download completed")
                showToast(R.string.info_download_completed)
                stopSelf()
            } else {
                updateNotification()
            }
        }
    }

    private fun showToast(@StringRes stringId: Int, length: Int = Toast.LENGTH_LONG) =
        GlobalScope.launch(TestableDispatchers.Main) {
            Toast.makeText(
                this@DownloadService,
                stringId,
                length
            ).show()
        }

    private lateinit var api: StcApi

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MyLog.i("onStartCommand")
        startForeground(NOTIFICATION_ID, createNotification().build())
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

    private fun createNotification(): NotificationCompat.Builder {
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
        val stopIntent = ActionActivity.stopDownloadIntent(this)
        val stopPendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(stopIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return NotificationCompat.Builder(this, Application.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.downloading))
            .addAction(
                R.mipmap.ic_launcher, getString(R.string.stop),
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)!!
    }

    private fun updateNotification() {
        val notification = createNotification()
            .setContentText(
                resources.getQuantityString(
                    R.plurals.info_downloading,
                    queuedIds.size,
                    completeIds.size + 1,
                    queuedIds.size
                )
            )
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private suspend fun onHandleIntent(intent: Intent?) = withContext(TestableDispatchers.IO) {
        intent!!.getStringExtra(EXTRA_URL)?.let { url ->
            api = get(parameters = { parametersOf(url) })
            try {
                val shareInfo = api.info()

                if (shareInfo == null) {
                    stopSelf()
                    throw NullPointerException("No share info data")
                }

                MyLog.d("Prepare to download $shareInfo")
                shareInfo.files.forEachIndexed { index, request ->
                    val response = api.file(index)

                    val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(path, "share_${System.currentTimeMillis()}").run {
                        this.mkdirs()
                        return@run File(path, request.filename)
                    }
                    MyLog.d("Writing to ${file.absolutePath}")

                    response.byteStream().use { input ->
                        FileOutputStream(file).use { output ->
                            val fileData = ByteArray(1024000)
                            var count = 1
                            while (count > 0) {
                                count = input.read(fileData)
                                if (count > 0)
                                    output.write(fileData, 0, count)
                            }
                        }
                    }


                }
//                getSystemService(DownloadManager::class.java)?.let { downloadManager ->
//                    requests.forEachIndexed { index, request ->
//                        val id = downloadManager.enqueue(request)
//                        queuedIds.add(id)
//                        MyLog.i("*enqueue[$index]: $id")
//                    }
//                    updateNotification()
//                }
            } catch (e: Exception) {
                MyLog.e(e.message ?: "", e)
                showToast(R.string.error_failed_web_upload)
                stopSelf()
            }
        }
    }

    private fun getDownloadRequests(
        url: String,
        shareInfo: ShareInfo
    ): List<DownloadManager.Request> {
        return shareInfo.files.mapIndexed { index, fileInfo ->
            val uri = Uri.parse("$url/$index")
            return@mapIndexed DownloadManager.Request(uri).apply {
                this.setTitle(fileInfo.filename)
                this.setDescription(getString(R.string.downloading))
                this.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                this.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileInfo.filename
                )
            }
        }
    }

    private fun downloadInfo(client: String): ShareInfo? {
        return try {
            val url = URL("$client/info")
            val inputStream = InputStreamReader(url.openStream())

            Gson().fromJson(inputStream, ShareInfo::class.java)
        } catch (e: Throwable) {
            MyLog.e("Error on downloading and parsing info", e)
            null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {

        private const val EXTRA_URL = "url"
        private const val NOTIFICATION_ID = 4519

        fun createIntent(context: Context, url: String?): Intent {
            return Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }

    }

}
