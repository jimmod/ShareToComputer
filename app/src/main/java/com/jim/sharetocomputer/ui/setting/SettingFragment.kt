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

package com.jim.sharetocomputer.ui.setting

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
import com.jim.sharetocomputer.Application
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.ext.getAppVersionCode
import com.jim.sharetocomputer.ext.getAppVersionName
import com.jim.sharetocomputer.ext.getIp
import com.jim.sharetocomputer.ext.isOnWifi
import com.jim.sharetocomputer.logging.MyLog
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.*


class SettingFragment : PreferenceFragmentCompat() {

    private val navigation by inject<SettingNavigation>(parameters = { parametersOf(this) })

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
        when (preference.title) {
            getString(R.string.title_download_log_preference) -> copyLogFile()
            getString(R.string.title_send_feedback_preference) -> sendFeedback()
            getString(R.string.title_about_preference) -> navigation.openAboutScreen()
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

    companion object {

        fun newInstance() = SettingFragment()

    }

}