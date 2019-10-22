package com.jim.sharetocomputer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jim.sharetocomputer.logging.MyLog

class ActionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent?.action
        MyLog.i("onCreate action: $action")
        if (action == ACTION_STOP_SHARE) {
            MyLog.i("*Stopping web server service")
            stopService(WebServerService.createIntent(this, null))
        } else if (action == ACTION_STOP_DOWNLOAD) {
            MyLog.i("*Stopping download service")
            stopService(DownloadService.createIntent(this, null))
        }
        finish()
    }

    companion object {
        const val ACTION_STOP_SHARE = "com.jim.sharetocomputer.STOP_SHARE"
        const val ACTION_STOP_DOWNLOAD = "com.jim.sharetocomputer.STOP_DOWNLOAD"

        fun stopShareIntent(context: Context) = Intent(context, ActionActivity::class.java).apply {
            action = ACTION_STOP_SHARE
        }

        fun stopDownloadIntent(context: Context) =
            Intent(context, ActionActivity::class.java).apply {
                action = ACTION_STOP_DOWNLOAD
            }

    }

}
