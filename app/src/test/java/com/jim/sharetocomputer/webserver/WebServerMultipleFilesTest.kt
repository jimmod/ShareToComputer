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
import com.jim.sharetocomputer.RobolectricApplication
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
        webServer.start()
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
        val uris = listOf<Uri>(
            Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/21"),
            Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/22"),
            Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/23")
        )

        shadowContentResolver.registerInputStream(uris[0], ByteArrayInputStream(SAMPLE_TEXT))
        shadowContentResolver.registerInputStream(uris[1], ByteArrayInputStream(SAMPLE_TEXT2))
        shadowContentResolver.registerInputStream(uris[2], ByteArrayInputStream(SAMPLE_TEXT3))
        val totalSize = (SAMPLE_TEXT.size + SAMPLE_TEXT2.size + SAMPLE_TEXT3.size).toLong()

        webServer.setUris(uris)

        val (code, file) = httpGet(TEST_URL)

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

    private fun httpGet(url: String): Pair<Int, File?> {
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

    companion object {
        private const val TEST_PORT = 8080
        private const val TEST_URL = "http://localhost:$TEST_PORT"

        private val SAMPLE_TEXT = "Hello World".toByteArray(Charsets.UTF_8)
        private val SAMPLE_TEXT2 = "Another Random World".toByteArray(Charsets.UTF_8)
        private val SAMPLE_TEXT3 = "N/A".toByteArray(Charsets.UTF_8)

    }
}