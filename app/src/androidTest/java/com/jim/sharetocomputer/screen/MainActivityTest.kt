package com.jim.sharetocomputer.screen

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import com.jim.sharetocomputer.MainActivity
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.permissionGrant
import org.junit.Rule
import org.junit.Test
import java.io.File


class MainActivityUiTest {

    @get:Rule
    val rule = IntentsTestRule(MainActivity::class.java)

    @get:Rule
    val grant = permissionGrant()

    @Test
    fun screen_sharing() {
        setupDummyImageSelect()
        assertMainScreenIsDisplayed()
        Screenshot.take("screen_main")

        clickShareImage()

        assertSharingScreenIsDisplayed()

        Screenshot.take("screen_sharing")

        clickStopShare()
    }

    @Test
    fun screen_about() {
        clickDrawerMenu(R.id.fragment_about)

        assertAboutScreenIsDisplayed()
        Screenshot.take("screen_about")
    }

    @Test
    fun screen_setting() {
        clickDrawerMenu(R.id.fragment_setting)

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

    private fun clickDrawerMenu(@IdRes id: Int) {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(id))
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