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

import androidx.fragment.app.testing.launchFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.jim.sharetocomputer.ui.main.MainFragmentDirections
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class SettingNavigationTest {

    @Test
    fun openAboutScreen() {
        val fragmentScenario = launchFragment<SettingFragment>()
        fragmentScenario.onFragment { fragment ->
            val navigation = SettingNavigation(fragment)
            val controller = Mockito.mock(NavController::class.java)

            Navigation.setViewNavController(fragment.requireView(), controller)

            navigation.openAboutScreen()

            Mockito.verify(controller).navigate(MainFragmentDirections.actionFragmentMainToFragmentAbout())
        }
    }

}