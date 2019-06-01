package com.jim.sharetocomputer

import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.io.File

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @After
    fun after() {
        stopKoin()
    }

    @Test
    fun open_launcher_should_not_start_service() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
        controller.setup()

        val service = Shadows.shadowOf(controller.get()).nextStartedService
        assertNull(service)
    }

    @Test
    fun share_text_should_start_service() {
        val sampleText = "Hello World"
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = ClipDescription.MIMETYPE_TEXT_PLAIN
            putExtra(Intent.EXTRA_TEXT, sampleText)
        }
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        controller.setup()

        val service = Shadows.shadowOf(controller.get()).nextStartedService

        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), ShareRequest.ShareRequestText(sampleText))
        assertThat(service, IntentMatchers.hasComponent(expectedIntent.component!!.className))
        expectedIntent.extras!!.keySet().forEach { key ->
            val expectedValue = expectedIntent.getParcelableExtra<ShareRequest>(key)
            assertThat(service, IntentMatchers.hasExtra(key, expectedValue))
        }
    }

    @Test
    fun share_single_file_should_start_service() {
        val uri = Uri.fromFile(File.createTempFile("temp","del"))
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        controller.setup()

        val service = Shadows.shadowOf(controller.get()).nextStartedService

        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), ShareRequest.ShareRequestSingleFile(uri))
        assertThat(service, IntentMatchers.hasComponent(expectedIntent.component!!.className))
        expectedIntent.extras!!.keySet().forEach { key ->
            val expectedValue = expectedIntent.getParcelableExtra<ShareRequest>(key)
            assertThat(service, IntentMatchers.hasExtra(key, expectedValue))
        }
    }

    @Test
    fun share_multiple_file_should_start_service() {
        val uris = arrayListOf(
            Uri.fromFile(File.createTempFile("temp","del")),
            Uri.fromFile(File.createTempFile("temp2","del"))
        )
        val intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        controller.setup()

        val service = Shadows.shadowOf(controller.get()).nextStartedService

        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), ShareRequest.ShareRequestMultipleFile(uris))
        assertThat(service, IntentMatchers.hasComponent(expectedIntent.component!!.className))
        expectedIntent.extras!!.keySet().forEach { key ->
            val expectedValue = expectedIntent.getParcelableExtra<ShareRequest>(key)
            assertThat(service, IntentMatchers.hasExtra(key, expectedValue))
        }
    }
}