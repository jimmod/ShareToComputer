package com.jim.sharetocomputer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jim.sharetocomputer.ext.getAppVersionCode
import com.jim.sharetocomputer.ext.getAppVersionName
import com.jim.sharetocomputer.ext.getIp
import com.jim.sharetocomputer.ext.isOnWifi
import com.jim.sharetocomputer.logging.MyLog
import java.io.*


class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MyLog.i("onCreate")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        MyLog.i("onDestroy")
        super.onDestroyView()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "download_log" -> copyLogFile()
            "send_feedback" -> sendFeedback()
            else -> return super.onPreferenceTreeClick(preference)
        }
        return true
    }

    private fun sendFeedback() {
        val logFileUri = FileProvider.getUriForFile(
            activity!!, "com.jim.sharetocomputer.provider",
            File(MyLog.logFilePath())
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(Application.EMAIL_ADDRESS))
            putExtra(Intent.EXTRA_TEXT, getDeviceInfo(activity!!))
            putExtra(Intent.EXTRA_SUBJECT, activity!!.getString(R.string.feedback_mail_title))
            putExtra(Intent.EXTRA_STREAM, logFileUri)
        }
        if (intent.resolveActivity(activity!!.packageManager) != null) {
            startActivity(intent)
        } else {
            MyLog.w("No mail application found")
            Toast.makeText(activity, R.string.error_fail_send_feedback, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDeviceInfo(context: Context): String? {
        val info = StringBuilder("${context.getString(R.string.feedback_email_body)}\n\n--------------------\n")
        info.append("Version: ${context.getAppVersionName()}(${context.getAppVersionCode()})\n")
        info.append("Phone: ${Build.BRAND}|${Build.MODEL}|${Build.BOARD}|${Build.DEVICE}\n")
        info.append("Wifi: ${context.isOnWifi()}|${context.getIp()}\n")
        return info.toString()
    }

    private fun copyLogFile() {
        val source = File(MyLog.logFilePath())
        val target =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), MyLog.LOG_FILE)
        try {
            copyFile(source, target)
            Toast.makeText(activity, R.string.info_download_log_complete, Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            MyLog.w("Fail copying log", e)
        }
    }

    private fun copyFile(source: File, target: File) {
        BufferedInputStream(FileInputStream(source)).use { input ->
            val data = input.readBytes()
            BufferedOutputStream(FileOutputStream(target)).use { output ->
                output.write(data)
            }
        }
    }

}