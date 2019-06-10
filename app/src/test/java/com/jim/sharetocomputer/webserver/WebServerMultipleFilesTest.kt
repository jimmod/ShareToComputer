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
import com.jim.sharetocomputer.RobolectricApplication
import com.jim.sharetocomputer.ShareInfo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(application = RobolectricApplication::class)
class WebServerMultipleFilesTest {

    private val webServer by lazy {
        WebServerMultipleFiles(application, TEST_PORT)
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
        uris.forEachIndexed { index, uri ->
            shadowContentResolver.registerInputStream(uri, ByteArrayInputStream(SAMPLE_TEXT[index]))
        }

        webServer.start()
    }

    @After
    fun end() {
        webServer.stop()
    }

    @Test
    fun default_content() {
        val (code, _) = httpGetFile(TEST_URL)
        Assert.assertEquals(404, code)
    }

    @Test
    fun get_content_as_zip() {
        webServer.setUris(uris)

        val (code, file) = httpGetFile(TEST_URL_ZIP)
        Assert.assertEquals(200, code)
        Assert.assertNotNull(file)
        val zipFile = ZipFile(file).entries().toList()
        Assert.assertEquals(3, zipFile.size)
        var actualTotalSize = 0L
        zipFile.forEach {
            actualTotalSize += it.size
        }
        Assert.assertEquals(totalSize, actualTotalSize)
    }

    @Test
    fun get_content_one_by_one() {
        webServer.setUris(uris)

        uris.forEachIndexed { index, _ ->
            val (code, content) = httpGetContent(testUrlIndex(index))
            Assert.assertEquals(200, code)
            Assert.assertNotNull(content)
            Assert.assertEquals(String(SAMPLE_TEXT[index]), String(content!!))
        }

    }

    @Test
    fun get_content_one_by_one_wrong_index() {
        webServer.setUris(uris)

        val (code, _) = httpGetContent(testUrlIndex(10))
        Assert.assertEquals(404, code)

    }

    @Test
    fun get_content_info() {
        webServer.setUris(uris)

        val (code, file) = httpGetFile(TEST_URL_INFO)
        Assert.assertEquals(200, code)
        val shareInfo = Gson().fromJson(FileReader(file), ShareInfo::class.java)
        Assert.assertEquals(uris.size, shareInfo.total)
    }

    @Test
    fun display_main_page() {
        webServer.setUris(uris)

        val (code, _) = httpGetContent(TEST_URL)
        Assert.assertEquals(200, code)
    }

    private fun httpGetFile(url: String): Pair<Int, File?> {
        val obj = URL(url)
        val con = obj.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        val code = con.responseCode
        var content: ByteArray? = null
        val file = File.createTempFile("sample", "test")
        try {
            BufferedInputStream(con.inputStream).use {
                content = it.readBytes()
            }
            BufferedOutputStream(FileOutputStream(file)).use {
                it.write(content)
            }
        } catch (e: Exception) {
        }
        return Pair(code, file)
    }
    private fun httpGetContent(url: String): Pair<Int, ByteArray?> {
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
        private const val TEST_URL_ZIP = "$TEST_URL/zip"
        private const val TEST_URL_INFO = "$TEST_URL/info"
        private fun testUrlIndex(index: Int) = "$TEST_URL/$index"

        private val SAMPLE_TEXT = arrayOf(
            "Hello World".toByteArray(Charsets.UTF_8),
            "Another Random World".toByteArray(Charsets.UTF_8),
            "N/A".toByteArray(Charsets.UTF_8)
        )

        val uris = listOf<Uri>(
            Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/21"),
            Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/22"),
            Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/23")
        )

        val totalSize = (SAMPLE_TEXT[0].size + SAMPLE_TEXT[1].size + SAMPLE_TEXT[2].size).toLong()

    }
}