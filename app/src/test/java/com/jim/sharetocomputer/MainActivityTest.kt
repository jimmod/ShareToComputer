package com.jim.sharetocomputer

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(application = RobolectricApplication::class)
class MainActivityTest {

    @get:Rule
    val uiRule = IntentsTestRule(MainActivity::class.java, false, false)

    private val activity by lazy {uiRule.activity}

    @Before
    fun before() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(applicationModule)
        }
    }

    @After
    fun after() {
        stopKoin()
        uiRule.finishActivity()
    }

    @Test
    fun open_launcher_should_not_start_service() {
        launchActivity()

        assertServiceNotStarted()
    }

    @Test
    fun share_text_should_start_service() {
        launchActivity(intentShareText)

        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), ShareRequest.ShareRequestText(text))
        assertServiceStarted(expectedIntent)
    }

    @Test
    fun share_single_file_should_start_service() {
        launchActivity(intentShareUri)

        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), ShareRequest.ShareRequestSingleFile(uri))
        assertServiceStarted(expectedIntent)
    }

    @Test
    fun share_multiple_file_should_start_service() {
        launchActivity(intentShareUris)

        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), ShareRequest.ShareRequestMultipleFile(uris))
        assertServiceStarted(expectedIntent)
    }

    @Test
    fun click_select_button_single_file() {
        launchActivity()
        setupDummyForActionGetContent_singleFile()

        pressSelectButton()

        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), ShareRequest.ShareRequestSingleFile(uri))
        assertServiceStarted(expectedIntent)
    }

    @Test
    fun click_select_button_multiple_files() {
        launchActivity()
        setupDummyForActionGetContent_multipleFiles()

        pressSelectButton()

        val expectedIntent = WebServerService.createIntent(ApplicationProvider.getApplicationContext(), ShareRequest.ShareRequestMultipleFile(uris))
        assertServiceStarted(expectedIntent)
    }

    private fun setupDummyForActionGetContent_singleFile() {
        val resultIntent = Intent().apply {
            data = uri
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result)
    }

    private fun setupDummyForActionGetContent_multipleFiles() {
        val resultIntent = Intent().apply {
            val item = ClipData.Item(uris[0])
            clipData = ClipData("", emptyArray(), item).apply {
                addItem(ClipData.Item(uris[1]))
            }
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result)
    }

    private fun launchActivity(intent: Intent? = null) {
        uiRule.launchActivity(intent)
    }

    private fun pressSelectButton() {
        onView(withId(R.id.select_file)).perform(click())
    }

    private fun assertServiceStarted(expectedIntent: Intent) {
        val serviceIntent = Shadows.shadowOf(activity).nextStartedService
        assertThat(serviceIntent, ExtraMatchers.sameComponentAs(expectedIntent))
        assertThat(serviceIntent, ExtraMatchers.sameExtrasAs(expectedIntent))
    }

    private fun assertServiceNotStarted() {
        val serviceIntent = Shadows.shadowOf(activity).nextStartedService
        assertThat(serviceIntent, Matchers.nullValue())
    }

    companion object {
        private const val text = "Hello World"
        private val intentShareText = Intent().apply {
            action = Intent.ACTION_SEND
            type = ClipDescription.MIMETYPE_TEXT_PLAIN
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val uri = Uri.fromFile(File.createTempFile("temp","del"))!!
        val intentShareUri = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val uris = arrayListOf(
            Uri.fromFile(File.createTempFile("temp","del")),
            Uri.fromFile(File.createTempFile("temp2","del"))
        )
        val intentShareUris = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }

    }
}