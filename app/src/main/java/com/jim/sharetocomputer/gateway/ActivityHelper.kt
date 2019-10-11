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

package com.jim.sharetocomputer.gateway

import android.app.Instrumentation
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.jim.sharetocomputer.AllOpen
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.ext.FragmentHelper
import com.jim.sharetocomputer.logging.MyLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AllOpen
class ActivityHelper {

    fun startActivityForResult(
        activity: FragmentActivity,
        intent: Intent
    ): Instrumentation.ActivityResult? {
        if (activity.isFinishing) {
            return null
        }
        val result = CompletableDeferred<Instrumentation.ActivityResult>()
        val requestCode = (Math.random() * 1000).toInt()

        var fragment = activity.supportFragmentManager.findFragmentByTag(TAG) as FragmentHelper?
        if (fragment == null) {
            fragment = FragmentHelper()
            GlobalScope.launch(TestableDispatchers.Main) {
                MyLog.d("Add headless fragment")
                activity.supportFragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commitNowAllowingStateLoss()
            }
        }
        fragment.addMapping(requestCode, result)

        GlobalScope.launch(TestableDispatchers.Main) {
            fragment.startActivityForResult(intent, requestCode)
        }
        return runBlocking {
            return@runBlocking result.await()
        }
    }

}

private const val TAG = "startActivityForResult"
