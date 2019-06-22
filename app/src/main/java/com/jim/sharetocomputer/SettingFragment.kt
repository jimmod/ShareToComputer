package com.jim.sharetocomputer

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
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
        if (preference.key == "download_log") {
            copyLogFile()
        }
        return super.onPreferenceTreeClick(preference)
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