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

package com.jim.sharetocomputer.ui.receive

import android.app.Application
import android.app.Instrumentation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.zxing.client.android.Intents.Scan
import com.jim.sharetocomputer.WebUploadService
import com.jim.sharetocomputer.gateway.ActivityHelper
import com.jim.sharetocomputer.ui.DummyActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class ReceiveNavigationTest {

    @get:Rule
    val rule = IntentsTestRule(DummyActivity::class.java)

    private val fragment = Fragment()

    @Before
    fun before() {
        rule.activity.supportFragmentManager.beginTransaction().apply {
            add(fragment, "fragment")
            commitNowAllowingStateLoss()
        }
    }

    @Test
    fun `openScanQrCode start zxing camera activity`() {
        val navigation = ReceiveNavigation(fragment, ActivityHelper())
        Intents.intending(IntentMatchers.anyIntent())
            .respondWith(Instrumentation.ActivityResult(AppCompatActivity.RESULT_CANCELED, null))

        runBlocking {
            navigation.openScanQrCode()
        }

        Intents.intended(IntentMatchers.hasAction(Scan.ACTION))
    }

    @Test
    fun start_web_upload_service() {
        val navigation = ReceiveNavigation(fragment, ActivityHelper())

        navigation.startWebUploadService()

        val app = ApplicationProvider.getApplicationContext<Application>()
        val startedService = Shadows.shadowOf(app).peekNextStartedService()

        Assert.assertThat(
            startedService,
            IntentMatchers.hasComponent(WebUploadService::class.java.name)
        )
    }


}