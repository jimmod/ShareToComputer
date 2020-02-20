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

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.jim.sharetocomputer.R
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations


class SendFragmentTest {

    @Mock
    lateinit var sendViewModel: SendViewModel

    private val testModule = module {
        single { sendViewModel }
    }

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private val isSharing = MutableLiveData<Boolean>()
    private val deviceIp = MutableLiveData<String>()
    private val devicePort = MutableLiveData<Int>()
    private val qrCodeDrawable = MutableLiveData<Drawable>()
    private val isAbleToShare = MutableLiveData<Boolean>()
    private val ip = "1.1.1.1"
    private val port = 1111
    private val address = "http://1.1.1.1:1111"

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        stopKoin()
        startKoin { modules(testModule) }

        Mockito.doReturn(isSharing).`when`(sendViewModel).isSharing()
        Mockito.doReturn(deviceIp).`when`(sendViewModel).deviceIp()
        Mockito.doReturn(devicePort).`when`(sendViewModel).devicePort()
        Mockito.doReturn(qrCodeDrawable).`when`(sendViewModel).qrCode()
        Mockito.doReturn(isAbleToShare).`when`(sendViewModel).isAbleToShare()
        UiThreadStatement.runOnUiThread {
            isSharing.value = false
            isAbleToShare.value = true
        }
    }

    @Test
    fun ui_displayed_no_share() {

        launchFragment()

        shareMediaButton().isDisplayed()
        shareFileButton().isDisplayed()
        stopShareButton().isNotDisplayed()
        qrCode().isNotDisplayed()
        url().isNotDisplayed()
    }

    @Test
    fun ui_displayed_share_is_on_going() {
        UiThreadStatement.runOnUiThread {
            isSharing.value = true
            deviceIp.value = ip
            devicePort.value = port
            qrCodeDrawable.value = application.getDrawable(R.drawable.abc_ab_share_pack_mtrl_alpha)
        }

        launchFragment()

        shareMediaButton().isNotDisplayed()
        shareFileButton().isNotDisplayed()
        stopShareButton().isDisplayed()
        qrCode().isDisplayed()
        url().withText(address)
    }

    @Test
    fun share_media_button_click() {
        launchFragment()
        shareMediaButton().click()

        Mockito.verify(sendViewModel).selectMedia()
    }

    @Test
    fun share_file_button_click() {
        launchFragment()
        shareFileButton().click()

        Mockito.verify(sendViewModel).selectFile()
    }

    @Test
    fun stop_share_button_click() {
        UiThreadStatement.runOnUiThread {
            isSharing.value = true
        }

        launchFragment()
        stopShareButton().click()

        Mockito.verify(sendViewModel).stopShare()
    }

    private fun shareMediaButton() = onView(withId(R.id.share_media))
    private fun shareFileButton() = onView(withText(R.string.share_file))
    private fun stopShareButton() = onView(withText(R.string.stop_share))
    private fun qrCode() = onView(withId(R.id.qrcode))
    private fun url() = onView(withId(R.id.url))

    private fun launchFragment() {
        launchFragmentInContainer<SendFragment>()
    }
}

private fun ViewInteraction.isDisplayed() {
    check(matches(ViewMatchers.isDisplayed()))
}

private fun ViewInteraction.isNotDisplayed() {
    check(matches(not(ViewMatchers.isDisplayed())))
}

private fun ViewInteraction.withText(s: String) {
    check(matches(ViewMatchers.withText(s)))
}

private fun ViewInteraction.click() {
    perform(ViewActions.click())
}
