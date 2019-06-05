package com.jim.sharetocomputer.coroutines

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object TestableDispatchers {

    @JvmStatic
    val Main: CoroutineDispatcher
        get() = mainDispatcher

    @JvmStatic
    val Default: CoroutineDispatcher
        get() = defaultDispatcher

    @JvmStatic
    val IO: CoroutineDispatcher
        get() = ioDispatcher

    @JvmStatic
    val Unconfined: CoroutineDispatcher
        get() = unconfinedDispatcher

    private var mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    private var ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private var unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @VisibleForTesting
    fun setMain(dispatcher: CoroutineDispatcher) {
        mainDispatcher = dispatcher
    }

    @VisibleForTesting
    fun setDefault(dispatcher: CoroutineDispatcher) {
        defaultDispatcher = dispatcher
    }

    @VisibleForTesting
    fun setIo(dispatcher: CoroutineDispatcher) {
        ioDispatcher = dispatcher
    }

    @VisibleForTesting
    fun setUnconfined(dispatcher: CoroutineDispatcher) {
        unconfinedDispatcher = dispatcher
    }


}