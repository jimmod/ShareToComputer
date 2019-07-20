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

package com.jim.sharetocomputer

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matchers
import org.junit.Test


class MainFragmentTest {


    @Test
    fun no_arguments() {
        launchFragmentInContainer<MainFragment>()

        onView(withId(R.id.layout_main)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.layout_sharing)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun share_image_in_arguments() {
        val args = MainFragment.createBundle(ShareRequest.ShareRequestText("Hello"))
        launchFragmentInContainer<MainFragment>(fragmentArgs = args)

        onView(withId(R.id.layout_main)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.layout_sharing)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.url)).check(matches(withText(Matchers.not(Matchers.containsString("unknown")))))
    }

}