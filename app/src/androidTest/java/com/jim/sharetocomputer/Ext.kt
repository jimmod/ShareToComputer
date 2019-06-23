package com.jim.sharetocomputer

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
