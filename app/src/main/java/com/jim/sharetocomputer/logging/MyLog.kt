package com.jim.sharetocomputer.logging

import android.content.Context
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator
import com.jim.sharetocomputer.BuildConfig

object MyLog {

    const val LOG_FILE = "mylog.log"

    fun d(msg: String) {
        XLog.tag(tag()).d(msg)
    }

    fun i(msg: String) {
        XLog.tag(tag()).i(msg)
    }

    fun w(msg: String, t: Throwable? = null) {
        if (t != null) {
            XLog.tag(tag()).w(msg, t)
        } else {
            XLog.tag(tag()).w(msg)
        }
    }

    fun e(msg: String, t: Throwable? = null) {
        if (t != null) {
            XLog.tag(tag()).e(msg, t)
        } else {
            XLog.tag(tag()).e(msg)
        }
    }

    fun setupLogging(context: Context) {
        val config = LogConfiguration.Builder()
            .logLevel(
                if (BuildConfig.DEBUG)
                    LogLevel.ALL
                else
                    LogLevel.INFO
            )
            .tag("[S2C]")
            .build()

        val androidPrinter = AndroidPrinter()
        val logFolder = context.filesDir.absolutePath + "/log"
        val filePrinter = FilePrinter.Builder(logFolder)
            .fileNameGenerator(ChangelessFileNameGenerator(LOG_FILE))        // Default: ChangelessFileNameGenerator("log")
            .flattener(ClassicFlattener())
            .build()

        XLog.init(
            config,
            androidPrinter,
            filePrinter
        )

    }

    private fun tag(): String {
        val st = Throwable().stackTrace.first {
            it.className != MyLog.javaClass.name
        }
        return "${st.className}[${st.lineNumber}]"
    }

}