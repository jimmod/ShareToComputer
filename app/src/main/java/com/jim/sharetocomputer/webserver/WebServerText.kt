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

import android.content.ClipDescription
import com.jim.sharetocomputer.Message
import timber.log.Timber

class WebServerText(port: Int): WebServer(port) {

    private var text: String? = null

    override fun serve(session: IHTTPSession?): Response {
        Timber.d("Incoming http request")
        if (text==null) return newFixedLengthResponse(Response.Status.NOT_FOUND, ClipDescription.MIMETYPE_TEXT_PLAIN, Message.ERROR_CONTENT_NOT_SET)
        return newFixedLengthResponse(Response.Status.OK, ClipDescription.MIMETYPE_TEXT_HTML, text)
    }

    fun setText(value: String) {
        text = value
    }

}