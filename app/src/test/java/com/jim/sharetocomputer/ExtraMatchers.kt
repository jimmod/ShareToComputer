package com.jim.sharetocomputer

import android.content.Intent
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

object ExtraMatchers {
    fun sameComponentAs(expectedIntent: Intent) =
        object : TypeSafeMatcher<Intent>() {
            override fun describeTo(description: Description) {
                description.appendText("has component: ${expectedIntent.component?.className}")
            }

            public override fun matchesSafely(intent: Intent): Boolean {
                return expectedIntent.component?.className.equals(intent.component?.className)
            }
        }

    fun sameExtrasAs(expectedIntent: Intent) =
        object : TypeSafeMatcher<Intent>() {
            override fun describeTo(description: Description) {
                description.appendText("has extras: ${expectedIntent.extras?.keySet()}")
            }

            public override fun matchesSafely(intent: Intent): Boolean {
                expectedIntent.extras?.keySet()?.forEach { key ->
                    if (expectedIntent.extras!!.get(key) != intent.extras!!.get(key)) return false
                }
                return true
            }
        }
}