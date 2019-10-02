package com.jim.sharetocomputer.logging

class MyUncaughtExceptionHandler(private val handler: Thread.UncaughtExceptionHandler) :
    Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        MyLog.e("uncaughtException", e)
        handler.uncaughtException(t, e)
    }
}