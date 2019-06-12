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

import android.app.DownloadManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.google.gson.Gson
import timber.log.Timber
import java.io.InputStreamReader
import java.net.URL

class DownloadIntentService : IntentService("DownloadIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        Timber.d("onHandleIntent")
        val url = intent!!.getStringExtra(EXTRA_URL)
        val shareInfo = downloadInfo(url)
        val requests = getDownloadRequests(url, shareInfo)

        val downloadManager = getSystemService(DownloadManager::class.java)
        requests.forEachIndexed { index, request ->
            val id = downloadManager.enqueue(request)
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
                this.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                this.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileInfo.filename)
            }
        }
    }

    private fun downloadInfo(client: String): ShareInfo {
        val URL = URL("$client/info")
        val inputStream = InputStreamReader(URL.openStream())

        return Gson().fromJson(inputStream, ShareInfo::class.java)
    }

    companion object {

        private const val EXTRA_URL = "url"

        fun createIntent(context: Context, url: String): Intent {
            return Intent(context, DownloadIntentService::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }

    }

}