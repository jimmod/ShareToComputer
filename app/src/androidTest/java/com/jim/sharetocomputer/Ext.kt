package com.jim.sharetocomputer

import android.Manifest
import androidx.test.rule.GrantPermissionRule

internal fun assertTimeout(timeout: Int, function: () -> Unit) {
    var run = true
    val loopUntilTimestamp = System.currentTimeMillis() + timeout
    while (System.currentTimeMillis() < loopUntilTimestamp && run) {
        try {
            function()
            run = false
        } catch (e: AssertionError) {
        }
    }
    function()
}

internal fun permissionGrant() = GrantPermissionRule.grant(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)!!