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

package com.jim.sharetocomputer.webserver

import android.app.Application
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


@RunWith(RobolectricTestRunner::class)
class WebServerReceiveTest {

    private val webServer by lazy {
        WebServerReceive(application, TEST_PORT, File("").toUri())
    }
    private val application by lazy {
        ApplicationProvider.getApplicationContext<Application>()
    }
    private val contentResolver by lazy {
        application.contentResolver
    }
    private val shadowContentResolver by lazy {
        Shadows.shadowOf(contentResolver)
    }

    @Before
    fun setup() {
        webServer.start()
    }

    @After
    fun end() {
        webServer.stop()
    }

    @Test
    fun default_content() {
        val (code, _) = httpGet(TEST_URL)
        assertEquals(200, code)
    }

    @Test
    fun upload_url_should_reject_http_get() {
        val (code, _) = httpGet(TEST_URL_UPLOAD)
        assertEquals(405, code)
    }

    private fun httpGet(url: String): Pair<Int, ByteArray?> {
        val obj = URL(url)
        val con = obj.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        val code = con.responseCode
        var content: ByteArray? = null
        try {
            BufferedInputStream(con.inputStream).use {
                content = it.readBytes()
            }
        } catch (e: Exception) {
        }
        return Pair(code, content)
    }

    companion object {
        private const val TEST_PORT = 8080
        private const val TEST_URL = "http://localhost:$TEST_PORT"
        private const val TEST_URL_UPLOAD = "http://localhost:$TEST_PORT/upload"


    }

}
