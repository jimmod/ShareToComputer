package com.jim.sharetocomputer

import com.jim.sharetocomputer.coroutines.DirectDispatcher
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        setupCoroutinesDispatchers()
    }

    private fun setupCoroutinesDispatchers() {
        TestableDispatchers.setDefault(DirectDispatcher())
        TestableDispatchers.setIo(DirectDispatcher())
        TestableDispatchers.setUnconfined(DirectDispatcher())
    }
}