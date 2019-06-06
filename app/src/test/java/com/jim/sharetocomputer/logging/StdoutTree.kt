package com.jim.sharetocomputer.logging

import android.util.Log
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class StdoutTree: Timber.DebugTree() {

    private val datetime = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG)

    override fun createStackElementTag(element: StackTraceElement): String {
        return "${super.createStackElementTag(element)}[${element.lineNumber}]"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val p = when (priority) {
            Log.DEBUG -> "DEBUG"
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            else -> priority.toString()
        }
        println("${datetime.format(Date())}|[$p][$tag] $message")
        t?.printStackTrace()
    }
}