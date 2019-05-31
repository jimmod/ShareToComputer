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

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ShareRequest: Parcelable {

    @Parcelize
    data class ShareRequestText(
        val text: String
    ): ShareRequest()

    @Parcelize
    data class ShareRequestSingleFile(
        val uri: Uri
    ): ShareRequest()

    @Parcelize
    data class ShareRequestMultipleFile(
        val uris: List<Uri>
    ): ShareRequest()
}