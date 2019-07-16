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
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.jim.sharetocomputer.webserver.WebServerMultipleFiles
import com.jim.sharetocomputer.webserver.WebServerSingleFile
import com.jim.sharetocomputer.webserver.WebServerText
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.mock.declareMock
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import java.io.File

@RunWith(RobolectricTestRunner::class)
class WebServerServiceTest: KoinTest {

    private val application by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private val webServerText by inject<WebServerText>()
    private val webServerSingleFile by inject<WebServerSingleFile>()
    private val webServerMultipleFiles by inject<WebServerMultipleFiles>()

    private val testModule = module {
        single { webServerText }
        single { webServerSingleFile }
        single { webServerMultipleFiles }
    }

    @Before
    fun before() {
        startKoin {
            modules(testModule)
        }
        declareMock<WebServerText>()
        declareMock<WebServerSingleFile>()
        declareMock<WebServerMultipleFiles>()
        Mockito.`when`(webServerText.lastAccessTime).thenReturn(System.currentTimeMillis())
        Mockito.`when`(webServerSingleFile.lastAccessTime).thenReturn(System.currentTimeMillis())
        Mockito.`when`(webServerMultipleFiles.lastAccessTime).thenReturn(System.currentTimeMillis())
    }

    @After
    fun after() {
        stopKoin()
    }

    @Test
    fun intent_doesnt_contain_request() {
        val service = Robolectric.buildService(WebServerService::class.java).create()
        service.startCommand(0,0)

        val shadow = Shadows.shadowOf(service.get())
        Assert.assertEquals(true, shadow.isStoppedBySelf)
    }

    @Test
    fun intent_request_text() {
        val text = "Hello World"
        val request = ShareRequest.ShareRequestText(text)
        val service = Robolectric.buildService(WebServerService::class.java, WebServerService.createIntent(application, request)).create()
        service.startCommand(0,0)

        val shadow = Shadows.shadowOf(service.get())
        Assert.assertEquals(false, shadow.isStoppedBySelf)
        Mockito.verify(webServerText).setText(text)
        Mockito.verify(webServerText).start()
    }

    @Test
    fun intent_request_single_file() {
        val uri = Uri.fromFile(File.createTempFile("temp","del"))
        val request = ShareRequest.ShareRequestSingleFile(uri)
        val service = Robolectric.buildService(WebServerService::class.java, WebServerService.createIntent(application, request)).create()
        service.startCommand(0,0)

        val shadow = Shadows.shadowOf(service.get())
        Assert.assertEquals(false, shadow.isStoppedBySelf)
        Mockito.verify(webServerSingleFile).setUri(uri)
        Mockito.verify(webServerSingleFile).start()
    }

    @Test
    fun intent_request_multiple_files() {
        val uris = listOf<Uri>(
            Uri.fromFile(File.createTempFile("temp","del")),
            Uri.fromFile(File.createTempFile("temp","del")),
            Uri.fromFile(File.createTempFile("temp","del"))
        )
        val request = ShareRequest.ShareRequestMultipleFile(uris)
        val service = Robolectric.buildService(WebServerService::class.java, WebServerService.createIntent(application, request)).create()
        service.startCommand(0,0)

        val shadow = Shadows.shadowOf(service.get())
        Assert.assertEquals(false, shadow.isStoppedBySelf)
        Mockito.verify(webServerMultipleFiles).setUris(uris)
        Mockito.verify(webServerMultipleFiles).start()
    }

}