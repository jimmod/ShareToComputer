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

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.jim.sharetocomputer.ShareInfo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class WebServerSingleFileTest {

    private val webServer by lazy {
        WebServerSingleFile(application, TEST_PORT)
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
        shadowContentResolver.registerInputStream(uri, ByteArrayInputStream(SAMPLE_TEXT.toByteArray(Charsets.UTF_8)))
    }

    @After
    fun end() {
        webServer.stop()
    }

    @Test
    fun default_content() {
        val (code, _) = httpGet(TEST_URL)
        Assert.assertEquals(404, code)
    }

    @Test
    fun check_response_body() {
        webServer.setUri(uri)

        val (code, content) = httpGet(TEST_URL)

        Assert.assertEquals(200, code)
        Assert.assertNotNull(content)
        Assert.assertEquals(SAMPLE_TEXT, String(content!!))
    }

    @Test
    fun get_content_info() {
        webServer.setUri(uri)

        val (code, content) = httpGet(TEST_URL_INFO)
        Assert.assertEquals(200, code)
        val shareInfo = Gson().fromJson(String(content!!), ShareInfo::class.java)
        Assert.assertEquals(1, shareInfo.total)
        Assert.assertEquals(1, shareInfo.files.size)
        Assert.assertEquals("21", shareInfo.files[0].filename)
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
        private const val TEST_URL_INFO = "$TEST_URL/info"

        private const val SAMPLE_TEXT = "Hello World"

        private val uri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/21")


    }
}

