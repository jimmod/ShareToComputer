package com.jim.sharetocomputer.fastlane

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.permissionGrant
import com.jim.sharetocomputer.ui.main.MainActivity
import org.junit.Rule
import org.junit.Test
import java.io.File


class MainScreenTest {

    @get:Rule
    val rule = IntentsTestRule(MainActivity::class.java)

    @get:Rule
    val grant = permissionGrant()

    @Test
    fun screen_send() {
        setupDummyImageSelect()
        assertMainScreenIsDisplayed()
        Screenshot.take("screen_main")

        clickShareImage()

        assertSharingScreenIsDisplayed()

        Screenshot.take("screen_sharing")

        clickStopShare()
    }

    @Test
    fun screen_setting() {
        clickSettingTab()

        assertSettingScreenIsDisplayed()
        Screenshot.take("screen_setting")
    }

    private fun setupDummyImageSelect() {
        val resultIntent = Intent().apply {
            val item = ClipData.Item(uri)
            clipData = ClipData("", emptyArray(), item)
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_PICK)).respondWith(result)
    }

    private fun clickShareImage() {
        onView(withId(R.id.share_media)).perform(click())
    }

    private fun clickSettingTab() {
        onView(withText(R.string.tab_title_setting)).perform(click())
    }

    private fun assertAboutScreenIsDisplayed() {
        onView(withId(R.id.layout_about)).check(matches(isDisplayed()))
    }

    private fun assertMainScreenIsDisplayed() {
        onView(withId(R.id.layout_main)).check(matches(isDisplayed()))
    }

    private fun assertSharingScreenIsDisplayed() {
        onView(withId(R.id.layout_sharing)).check(matches(isDisplayed()))
    }

    private fun assertSettingScreenIsDisplayed() {
        onView(withText(R.string.title_send_feedback_preference)).check(matches(isDisplayed()))
    }


    private fun clickStopShare() {
        onView(withId(R.id.stop_share)).perform(click())
    }

    companion object {
        private val uri = Uri.fromFile(File.createTempFile("temp", "del"))!!
    }
}