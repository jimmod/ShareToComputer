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

import androidx.fragment.app.testing.launchFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jim.sharetocomputer.ui.main.MainFragmentDirections
import com.jim.sharetocomputer.ui.setting.SettingFragment
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class ReceiveNavigationTest {

    @Test
    fun open_scan_qr_code_fragment() {
        val fragmentScenario = launchFragment<SettingFragment>()
        fragmentScenario.onFragment { fragment ->
            val navigation = ReceiveNavigation(fragment)
            val controller = Mockito.mock(NavController::class.java)

            Navigation.setViewNavController(fragment.requireView(), controller)

            navigation.openScanQrCode()

            Mockito.verify(controller).navigate(MainFragmentDirections.actionFragmentMainToFragmentQrcode())
        }
    }

}