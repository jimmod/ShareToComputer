package com.jim.sharetocomputer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class ActionActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent?.action
        Timber.d("onCreate action: $action")
        if (action == ACTION_STOP_SHARE) {
            Timber.i("Stopping service")
            stopService(WebServerService.createIntent(this, null))
        }
        finish()
    }

    companion object {
        const val ACTION_STOP_SHARE = "com.jim.sharetocomputer.STOP_SHARE"

        fun stopShareIntent(context:Context) = Intent(context, ActionActivity::class.java).apply {
            action = ACTION_STOP_SHARE
        }

    }

}