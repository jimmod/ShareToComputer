package com.jim.sharetocomputer

import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test


class MainActivityUiTest {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun open_launcher() {
        clickDrawerMenu(R.id.fragment_about)

        assertAboutScreenIsDisplayed()
    }

    private fun clickDrawerMenu(@IdRes id : Int) {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(id))
    }

    private fun assertAboutScreenIsDisplayed() {
        onView(withId(R.id.layout_about)).check(matches(isDisplayed()))
    }
}