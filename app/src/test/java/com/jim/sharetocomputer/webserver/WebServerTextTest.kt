/*
    This file is part of Share To Computer  Copyright (C) 2019  Jimmy <https://github.com/jimmod/ShareToComputer>.

    Share To Computer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Share To Computer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Share To Computer.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.jim.sharetocomputer.webserver

import com.google.gson.Gson
import com.jim.sharetocomputer.ShareInfo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


@RunWith(RobolectricTestRunner::class)
class WebServerTextTest {

    private lateinit var webServer: WebServerText

    @Before
    fun setup() {
        webServer = WebServerText(TEST_PORT)
        webServer.start()
    }

    @After
    fun end() {
        webServer.stop()
    }

    @Test
    fun default_body() {
        val (code, _) = httpGet(TEST_URL)

        Assert.assertEquals(404, code)
    }

    @Test
    fun check_response_body() {
        webServer.setText(SAMPLE_TEXT)

        val (code, body) = httpGet(TEST_URL)

        Assert.assertEquals(200, code)
        Assert.assertEquals(SAMPLE_TEXT, body)
    }

    @Test
    fun get_content_info() {
        webServer.setText(SAMPLE_TEXT)

        val (code, content) = httpGet(TEST_URL_INFO)
        Assert.assertEquals(200, code)
        val shareInfo = Gson().fromJson(content, ShareInfo::class.java)
        Assert.assertEquals(1, shareInfo.total)
        Assert.assertEquals(1, shareInfo.files.size)
        Assert.assertTrue(shareInfo.files[0].filename.matches("[0-9]+\\.txt".toRegex()))
    }

    private fun httpGet(url: String): Pair<Int, String> {
        val obj = URL(url)
        val con = obj.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        val code = con.responseCode
        val sb = StringBuilder()
        try {
            BufferedReader(InputStreamReader(con.inputStream)).useLines { lines ->
                lines.forEach { line ->
                    sb.append(line)
                }
            }
        } catch (e: Exception) {
        }
        val body = sb.toString()
        return Pair(code, body)
    }

    companion object {
        private const val TEST_PORT = 8080
        private const val TEST_URL = "http://localhost:$TEST_PORT"
        private const val TEST_URL_INFO = "$TEST_URL/info"

        private const val SAMPLE_TEXT = "Hello World"

    }

}
