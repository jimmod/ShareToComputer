package com.jim.sharetocomputer

import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
    }

}