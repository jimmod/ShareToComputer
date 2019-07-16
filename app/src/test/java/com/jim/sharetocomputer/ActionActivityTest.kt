package com.jim.sharetocomputer

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.rule.IntentsTestRule
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class ActionActivityTest {

    @get:Rule
    val uiRule = IntentsTestRule(ActionActivity::class.java, false, false)

    private val application: Context by lazy { ApplicationProvider.getApplicationContext<Application>() }

    @Test
    fun stop_share() {
        val intent = ActionActivity.stopShareIntent(application)
        uiRule.launchActivity(intent)

        assertWebServerServiceStopped()
    }

    @Test
    fun stop_download() {
        val intent = ActionActivity.stopDownloadIntent(application)
        uiRule.launchActivity(intent)

        assertDownloadServiceStopped()
    }

    private fun assertWebServerServiceStopped() {
        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), null)
        val serviceIntent = Shadows.shadowOf(uiRule.activity).nextStoppedService
        assertThat(serviceIntent, ExtraMatchers.sameComponentAs(expectedIntent))
    }

    private fun assertDownloadServiceStopped() {
        val expectedIntent = DownloadService.createIntent(ApplicationProvider.getApplicationContext(), null)
        val serviceIntent = Shadows.shadowOf(uiRule.activity).nextStoppedService
        assertThat(serviceIntent, ExtraMatchers.sameComponentAs(expectedIntent))
    }

}