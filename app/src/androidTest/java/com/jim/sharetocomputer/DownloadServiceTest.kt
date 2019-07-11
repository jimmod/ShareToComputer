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
package com.jim.sharetocomputer

import android.app.Application
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.jim.sharetocomputer.logging.MyLog
import fi.iki.elonen.NanoHTTPD
import org.junit.*
import java.io.File

@Ignore("Failed in travis-ci")
class DownloadServiceTest {
    @get:Rule
    val grant = permissionGrant()

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private lateinit var webserver: NanoHTTPD
    private val downloadFolder by lazy { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) }
    private val fileTemp by lazy {
        arrayOf(
            File(downloadFolder, FILENAME_TEXT),
            File(downloadFolder, FILENAME_PNG),
            File(downloadFolder, FILENAME_PNG2)
        )
    }

    @Before
    fun before() {
        cleanTempFile()
    }

    @After
    fun after() {
        if (::webserver.isInitialized) webserver.stop()
        cleanTempFile()
    }

    private fun cleanTempFile() {
        fileTemp.forEach {
            val result = it.delete()
            if (!result) MyLog.e("Fail delete ${it.absolutePath}")
            else MyLog.e("Success delete ${it.absolutePath}")
        }
        assertTimeout(1000) {
            fileTemp.forEach {
                Assert.assertEquals(false, it.exists())
            }
        }
    }

    @Test
    fun download_single_file() {
        webserver = MockServerSingleFile().apply { start() }

        val intent = DownloadService.createIntent(application, URL)
        application.startService(intent)

        val actualFile = File(downloadFolder, FILENAME_PNG)
        assertTimeout(2000) {
            Assert.assertEquals(true, actualFile.exists())
        }
    }

    @Test
    fun download_text_content() {
        webserver = MockServerTextContent().apply { start() }

        val intent = DownloadService.createIntent(application, URL)
        application.startService(intent)

        val actualFile = File(downloadFolder, FILENAME_TEXT)
        assertTimeout(2000) {
            Assert.assertEquals(true, actualFile.exists())
        }
    }

    @Test
    fun download_multiple_files() {
        webserver = MockServerMultipleFiles().apply { start() }

        val intent = DownloadService.createIntent(application, URL)
        application.startService(intent)

        val actualFile1 = File(downloadFolder, FILENAME_PNG)
        assertTimeout(2000) {
            Assert.assertEquals(true, actualFile1.exists())
        }
        val actualFile2 = File(downloadFolder, FILENAME_TEXT)
        assertTimeout(2000) {
            Assert.assertEquals(true, actualFile2.exists())
        }
    }

    @Test
    fun download_exist_filename_will_rename_it() {
        webserver = MockServerSingleFile().apply { start() }

        val intent = DownloadService.createIntent(application, URL)
        application.startService(intent)

        val actualFile = File(downloadFolder, FILENAME_PNG)
        assertTimeout(2000) {
            Assert.assertEquals(true, actualFile.exists())
        }

        val intent2 = DownloadService.createIntent(application, URL)
        application.startService(intent2)

        val actualFile2 = File(downloadFolder, FILENAME_PNG2)
        assertTimeout(2000) {
            Assert.assertEquals(true, actualFile2.exists())
        }
    }

    inner class MockServerSingleFile : NanoHTTPD(PORT) {

        override fun serve(session: IHTTPSession): Response {
            MyLog.d("$TAG incoming request")
            return if (session.uri == "/info") {
                newFixedLengthResponse(Response.Status.OK, "application/json", INFO_RESPONSE_SINGLE_FILE)
            } else {
                val inputStream = application.assets.open("web/logo.png")
                newFixedLengthResponse(Response.Status.OK, "image/png", inputStream, -1).apply {
                    addHeader("Content-Disposition", "filename=\"$FILENAME_PNG\"")
                }
            }
        }

    }

    inner class MockServerTextContent : NanoHTTPD(PORT) {

        override fun serve(session: IHTTPSession): Response {
            MyLog.d("$TAG incoming request")
            return if (session.uri == "/info") {
                newFixedLengthResponse(Response.Status.OK, "application/json", INFO_RESPONSE_TEXT)
            } else {
                val inputStream = application.assets.open("web/main.html")
                newFixedLengthResponse(Response.Status.OK, "text/html", inputStream, -1).apply {
                    addHeader("Content-Disposition", "filename=\"$FILENAME_TEXT\"")
                }
            }
        }

    }

    inner class MockServerMultipleFiles : NanoHTTPD(PORT) {

        override fun serve(session: IHTTPSession): Response {
            MyLog.d("$TAG incoming request")
            return when {
                session.uri == "/info" -> newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    INFO_RESPONSE_MULTIPLE_FILES
                )
                session.uri == "/0" -> {
                    val inputStream = application.assets.open("web/logo.png")
                    newFixedLengthResponse(Response.Status.OK, "image/png", inputStream, -1).apply {
                        addHeader("Content-Disposition", "filename=\"$FILENAME_PNG\"")
                    }
                }
                session.uri == "/1" -> {
                    val inputStream = application.assets.open("web/main.html")
                    newFixedLengthResponse(Response.Status.OK, "text/html", inputStream, -1).apply {
                        addHeader("Content-Disposition", "filename=\"$FILENAME_TEXT\"")
                    }
                }
                else -> newFixedLengthResponse(Response.Status.OK, "text/plain", "")
            }
        }

    }

    companion object {
        private const val TAG = "[DownloadServiceTest]"
        private const val TEMP_FOLDER = "tempTest"
        private const val PORT = 8080
        private const val URL = "http://localhost:$PORT"
        private const val FILENAME_TEXT = "sharetocomputer_sample.html"
        private const val FILENAME_PNG = "sharetocomputer_sample.png"
        private const val FILENAME_PNG2 = "sharetocomputer_sample-1.png"
        private val INFO_RESPONSE_TEXT = Gson().toJson(ShareInfo(1, listOf(FileInfo(FILENAME_TEXT))))
        private val INFO_RESPONSE_SINGLE_FILE = Gson().toJson(ShareInfo(1, listOf(FileInfo(FILENAME_PNG))))
        private val INFO_RESPONSE_MULTIPLE_FILES =
            Gson().toJson(ShareInfo(2, listOf(FileInfo(FILENAME_PNG), FileInfo(FILENAME_TEXT))))

    }

}