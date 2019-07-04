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

import java.io.InputStream

class InputStreamNotifyWebServer(private val wrappedInputStream: InputStream, private val webServer: WebServer) :
    InputStream() {

    override fun read(): Int {
        notifyWebServer()
        return wrappedInputStream.read()
    }

    override fun read(b: ByteArray?): Int {
        notifyWebServer()
        return wrappedInputStream.read(b)
    }

    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        notifyWebServer()
        return wrappedInputStream.read(b, off, len)
    }


    private fun notifyWebServer() {
        webServer.notifyAccess()
    }

}