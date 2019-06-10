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
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream

open class WebServer(port: Int): NanoHTTPD(port) {
    protected fun infoResponse(total: Int): Response {
        val shareInfo = ShareInfo(total)
        val inputStream = ByteArrayInputStream(Gson().toJson(shareInfo).toByteArray())
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            inputStream,
            -1
        )
    }
}