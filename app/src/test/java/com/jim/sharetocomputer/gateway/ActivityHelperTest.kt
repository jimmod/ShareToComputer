package com.jim.sharetocomputer.gateway

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import com.jim.sharetocomputer.ui.DummyActivity
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActivityHelperTest {

    @get:Rule
    val activityTestRule = IntentsTestRule(DummyActivity::class.java)

    @Test
    fun `Should send the correct intent`() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        Intents.intending(anyIntent()).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        )

        ActivityHelper().startActivityForResult(
            activity = activityTestRule.activity,
            intent = intent
        )

        Intents.intending(
            allOf(
                hasAction(Intent.ACTION_OPEN_DOCUMENT),
                hasCategories(setOf(Intent.CATEGORY_OPENABLE)),
                hasType("*/*"),
                hasExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            )
        )
    }

    @Test
    fun `Should return correct ActivityResult`() {
        val intentResult = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        Intents.intending(anyIntent()).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_OK, intentResult)
        )

        val result = ActivityHelper().startActivityForResult(
            activity = activityTestRule.activity,
            intent = Intent()
        )

        assertNotNull(result)
        assertThat(result!!.resultCode, equalTo(Activity.RESULT_OK))
        assertThat(
            result.resultData, allOf(
                hasAction(Intent.ACTION_OPEN_DOCUMENT),
                hasCategories(setOf(Intent.CATEGORY_OPENABLE)),
                hasType("*/*"),
                hasExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            )
        )
    }

}
