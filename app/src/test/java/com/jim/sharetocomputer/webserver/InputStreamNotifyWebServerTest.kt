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

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream

class InputStreamNotifyWebServerTest {

    private val inputStream = ByteArrayInputStream(ByteArray(10))
    private val webServer = WebServerMock()

    @Test
    fun check_notify_called_on_read() {
        val test = InputStreamNotifyWebServer(inputStream, webServer)
        test.read()

        Assert.assertEquals(true, webServer.isNotifyAccessIsCalled)
    }

    @Test
    fun check_notify_called_on_read2() {
        val test = InputStreamNotifyWebServer(inputStream, webServer)
        test.read(ByteArray(10))

        Assert.assertEquals(true, webServer.isNotifyAccessIsCalled)
    }

    @Test
    fun check_notify_called_on_read3() {
        val test = InputStreamNotifyWebServer(inputStream, webServer)
        test.read(ByteArray(10), 0, 10)

        Assert.assertEquals(true, webServer.isNotifyAccessIsCalled)
    }

}

private class WebServerMock : WebServer(8080) {

    var isNotifyAccessIsCalled = false

    override fun notifyAccess() {
        isNotifyAccessIsCalled = true
    }

}