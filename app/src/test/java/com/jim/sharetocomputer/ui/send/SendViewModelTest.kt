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

package com.jim.sharetocomputer.ui.send

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.ClipData
import android.content.Intent
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jim.sharetocomputer.ShareRequest
import com.jim.sharetocomputer.WebServerService
import com.jim.sharetocomputer.gateway.ActivityHelper
import com.jim.sharetocomputer.gateway.WifiApi
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.Matchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import java.io.File


@RunWith(AndroidJUnit4::class)
class SendViewModelTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val applicationShadow by lazy { Shadows.shadowOf(application) }
    private val wifiApi = Mockito.mock(WifiApi::class.java)
    private val activityHelper = Mockito.mock(ActivityHelper::class.java)
    private val sendViewModel by lazy { SendViewModel(application, wifiApi, activityHelper) }
    private val ip = "1.2.3.4"
    private val port = 1234

    @Before
    fun before() {
        Mockito.doReturn(ip).`when`(wifiApi).getIp()
        WebServerService.port.value = port
    }

    @Test
    fun default_state() {
        assertEquals(false, sendViewModel.isSharing().value)
        assertEquals(ip, sendViewModel.deviceIp().value)
        assertEquals(port, sendViewModel.devicePort().value)
    }

    @Test
    fun `selectFile() - given no file selected should not start WebServerService`() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        val intentResult = Intent()
        val activityResult = Instrumentation.ActivityResult(
            Activity.RESULT_CANCELED,
            intentResult
        )
        whenever(activityHelper.startActivityForResult(any(), any())).thenReturn(activityResult)

        sendViewModel.activity = activity
        sendViewModel.selectFile()

        val serviceStarted = applicationShadow.peekNextStartedService()
        assertNull(serviceStarted)
    }

    @Test
    fun `selectFile() - given single file selected should start WebServerService`() {
        val fileUri = File.createTempFile("sample", "test").toUri()
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        val intentResult = Intent().apply {
            data = fileUri
        }
        val activityResult = Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            intentResult
        )
        whenever(activityHelper.startActivityForResult(any(), any())).thenReturn(activityResult)

        sendViewModel.activity = activity
        sendViewModel.selectFile()

        val expectedIntent =
            WebServerService.createIntent(activity, ShareRequest.ShareRequestSingleFile(fileUri))
        val serviceStarted = applicationShadow.peekNextStartedService()
        assertThat(serviceStarted?.component, Matchers.equalTo(expectedIntent.component))
        expectedIntent.extras!!.keySet().forEach {
            assertThat(
                serviceStarted!!.extras!!.getParcelable<ShareRequest>(it),
                Matchers.equalTo(expectedIntent.extras?.getParcelable(it))
            )
        }
    }

    @Test
    fun `selectFile() - given single file selected should start WebServerService - 2`() {
        val fileUri = File.createTempFile("sample", "test").toUri()
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        val intentResult = Intent().apply {
            clipData = ClipData.newRawUri("1", fileUri)
        }
        val activityResult = Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            intentResult
        )
        whenever(activityHelper.startActivityForResult(any(), any())).thenReturn(activityResult)

        sendViewModel.activity = activity
        sendViewModel.selectFile()

        val expectedIntent =
            WebServerService.createIntent(activity, ShareRequest.ShareRequestSingleFile(fileUri))
        val serviceStarted = applicationShadow.peekNextStartedService()
        assertThat(serviceStarted?.component, Matchers.equalTo(expectedIntent.component))
        expectedIntent.extras!!.keySet().forEach {
            assertThat(
                serviceStarted!!.extras!!.getParcelable<ShareRequest>(it),
                Matchers.equalTo(expectedIntent.extras?.getParcelable(it))
            )
        }
    }

    @Test
    fun `selectFile() - given multiple files selected should start WebServerService`() {
        val fileUri = File.createTempFile("sample", "test").toUri()
        val fileUri2 = File.createTempFile("sample", "test2").toUri()
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        val intentResult = Intent().apply {
            clipData = ClipData.newRawUri("1", fileUri).apply {
                addItem(ClipData.Item(fileUri2))
            }
        }
        val activityResult = Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            intentResult
        )
        whenever(activityHelper.startActivityForResult(any(), any())).thenReturn(activityResult)

        sendViewModel.activity = activity
        sendViewModel.selectFile()

        val expectedIntent =
            WebServerService.createIntent(
                activity,
                ShareRequest.ShareRequestMultipleFile(listOf(fileUri, fileUri2))
            )
        val serviceStarted = applicationShadow.peekNextStartedService()
        assertThat(serviceStarted?.component, Matchers.equalTo(expectedIntent.component))
        expectedIntent.extras!!.keySet().forEach {
            assertThat(
                serviceStarted!!.extras!!.getParcelable<ShareRequest>(it),
                Matchers.equalTo(expectedIntent.extras?.getParcelable(it))
            )
        }
    }


    @Test
    fun `selectMedia() - given no file selected should not start WebServerService`() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        val intentResult = Intent()
        val activityResult = Instrumentation.ActivityResult(
            Activity.RESULT_CANCELED,
            intentResult
        )
        whenever(activityHelper.startActivityForResult(any(), any())).thenReturn(activityResult)

        sendViewModel.activity = activity
        sendViewModel.selectMedia()

        val serviceStarted = applicationShadow.peekNextStartedService()
        assertNull(serviceStarted)
    }


    @Test
    fun `selectMedia() - given single file selected should start WebServerService`() {
        val fileUri = File.createTempFile("sample", "test").toUri()
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        val intentResult = Intent().apply {
            data = fileUri
        }
        val activityResult = Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            intentResult
        )
        whenever(activityHelper.startActivityForResult(any(), any())).thenReturn(activityResult)

        sendViewModel.activity = activity
        sendViewModel.selectMedia()

        val expectedIntent =
            WebServerService.createIntent(activity, ShareRequest.ShareRequestSingleFile(fileUri))
        val serviceStarted = applicationShadow.peekNextStartedService()
        assertThat(serviceStarted?.component, Matchers.equalTo(expectedIntent.component))
        expectedIntent.extras!!.keySet().forEach {
            assertThat(
                serviceStarted!!.extras!!.getParcelable<ShareRequest>(it),
                Matchers.equalTo(expectedIntent.extras?.getParcelable(it))
            )
        }
    }

    @Test
    fun `selectMedia() - given single file selected should start WebServerService - 2`() {
        val fileUri = File.createTempFile("sample", "test").toUri()
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        val intentResult = Intent().apply {
            clipData = ClipData.newRawUri("1", fileUri)
        }
        val activityResult = Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            intentResult
        )
        whenever(activityHelper.startActivityForResult(any(), any())).thenReturn(activityResult)

        sendViewModel.activity = activity
        sendViewModel.selectMedia()

        val expectedIntent =
            WebServerService.createIntent(activity, ShareRequest.ShareRequestSingleFile(fileUri))
        val serviceStarted = applicationShadow.peekNextStartedService()
        assertThat(serviceStarted?.component, Matchers.equalTo(expectedIntent.component))
        expectedIntent.extras!!.keySet().forEach {
            assertThat(
                serviceStarted!!.extras!!.getParcelable<ShareRequest>(it),
                Matchers.equalTo(expectedIntent.extras?.getParcelable(it))
            )
        }
    }

    @Test
    fun `selectMedia() - given multiple files selected should start WebServerService`() {
        val fileUri = File.createTempFile("sample", "test").toUri()
        val fileUri2 = File.createTempFile("sample", "test2").toUri()
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        val intentResult = Intent().apply {
            clipData = ClipData.newRawUri("1", fileUri).apply {
                addItem(ClipData.Item(fileUri2))
            }
        }
        val activityResult = Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            intentResult
        )
        whenever(activityHelper.startActivityForResult(any(), any())).thenReturn(activityResult)

        sendViewModel.activity = activity
        sendViewModel.selectMedia()

        val expectedIntent =
            WebServerService.createIntent(
                activity,
                ShareRequest.ShareRequestMultipleFile(listOf(fileUri, fileUri2))
            )
        val serviceStarted = applicationShadow.peekNextStartedService()
        assertThat(serviceStarted?.component, Matchers.equalTo(expectedIntent.component))
        expectedIntent.extras!!.keySet().forEach {
            assertThat(
                serviceStarted!!.extras!!.getParcelable<ShareRequest>(it),
                Matchers.equalTo(expectedIntent.extras?.getParcelable(it))
            )
        }
    }
}
