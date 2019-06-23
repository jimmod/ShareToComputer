package com.jim.sharetocomputer

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.os.Environment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers
import org.junit.*
import java.io.File

class SettingFragmentTest {

    @get:Rule
    val grant = permissionGrant()

    @Before
    fun before() {
        Intents.init()
    }

    @After
    fun after() {
        Intents.release()
    }

    @Test
    fun all_setting_displayed() {
        launchFragmentInContainer<SettingFragment>()

        downloadLog().check(matches(isDisplayed()))
        sendFeedback().check(matches(isDisplayed()))
    }


    @Test
    fun send_feedback() {
        launchFragmentInContainer<SettingFragment>()
        Intents.intending(anyIntent()).respondWith(ActivityResult(Activity.RESULT_OK, null))

        sendFeedback().perform(click())

        Intents.intended(
            Matchers.allOf(
                hasAction(Intent.ACTION_SEND),
                hasType("message/rfc822"),
                hasExtra(Intent.EXTRA_EMAIL, arrayOf(Application.EMAIL_ADDRESS))
            )
        )
    }

    @Test
    fun download_log() {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "mylog.log")
        file.delete()
        assertTimeout(2000) {
            Assert.assertFalse(file.exists())
        }

        launchFragmentInContainer<SettingFragment>()

        downloadLog().perform(click())

        assertTimeout(2000) {
            Assert.assertTrue(file.exists())
        }
    }

    private fun downloadLog() = onView(withText(R.string.title_download_log_preference))
    private fun sendFeedback() = onView(withText(R.string.title_send_feedback_preference))

}