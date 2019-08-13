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

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.jim.sharetocomputer.R
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class ReceiveFragmentTest {

    @Mock
    lateinit var viewModel: ReceiveViewModel

    private val testModule = module {
        single { viewModel }
    }

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        stopKoin()
        startKoin { modules(testModule) }

    }

    @Test
    fun click_scan_qr_code() {
        launchFragmentInContainer<ReceiveFragment>()

        onView(withId(R.id.scan_qr_code)).perform(click())

        Mockito.verify(viewModel).scanQrCode()
    }

}