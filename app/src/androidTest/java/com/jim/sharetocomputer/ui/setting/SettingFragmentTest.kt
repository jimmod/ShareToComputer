/*
 *     This file is part of Share To Computer  Copyright (C) 2019  Jimmy <https://github.com/jimmod/ShareToComputer>.
 *
 *     Share To Computer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Share To Computer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Share To Computer.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.jim.sharetocomputer.ui.setting

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.os.Environment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.jim.sharetocomputer.Application
import com.jim.sharetocomputer.R
import com.jim.sharetocomputer.assertTimeout
import com.jim.sharetocomputer.permissionGrant
import org.hamcrest.Matchers
import org.junit.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.File

class SettingFragmentTest : KoinTest {

    @get:Rule
    val grant = permissionGrant()

    @Mock
    lateinit var navigation: SettingNavigation

    private val testModule = module {
        single { navigation }
    }

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        Intents.init()
        stopKoin()
        startKoin { modules(testModule) }
    }

    @After
    fun after() {
        Intents.release()
    }

    @Test
    fun all_setting_displayed() {
        launchFragment()

        downloadLog().isDisplayed()
        sendFeedback().isDisplayed()
        about().isDisplayed()
    }

    @Test
    fun send_feedback() {
        launchFragment()
        Intents.intending(anyIntent()).respondWith(ActivityResult(Activity.RESULT_OK, null))

        sendFeedback().click()

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

        launchFragment()

        downloadLog().click()

        assertTimeout(2000) {
            Assert.assertTrue(file.exists())
        }
    }

    @Test
    fun open_about() {
        launchFragment()

        about().click()

        Mockito.verify(navigation).openAboutScreen()
    }

    private fun launchFragment() {
        launchFragmentInContainer<SettingFragment>()
    }

    private fun downloadLog() = onView(withText(R.string.title_download_log_preference))
    private fun sendFeedback() = onView(withText(R.string.title_send_feedback_preference))
    private fun about() = onView(withText(R.string.title_about_preference))

    private fun ViewInteraction.click() = perform(ViewActions.click())
    private fun ViewInteraction.isDisplayed() = check(matches(ViewMatchers.isDisplayed()))

}