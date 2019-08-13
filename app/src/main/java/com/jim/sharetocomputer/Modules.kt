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

import androidx.fragment.app.Fragment
import com.jim.sharetocomputer.gateway.ActivityHelper
import com.jim.sharetocomputer.gateway.WifiApi
import com.jim.sharetocomputer.ui.main.MainViewModel
import com.jim.sharetocomputer.ui.send.SendViewModel
import com.jim.sharetocomputer.ui.setting.SettingNavigation
import com.jim.sharetocomputer.webserver.WebServerMultipleFiles
import com.jim.sharetocomputer.webserver.WebServerSingleFile
import com.jim.sharetocomputer.webserver.WebServerText
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module


val applicationModule = module {
    factory { (port: Int) -> WebServerText(port) }
    factory { (port: Int) -> WebServerSingleFile(get(), port) }
    factory { (port: Int) -> WebServerMultipleFiles(get(), port) }
    factory { (fragment: Fragment) -> SettingNavigation(fragment) }
    viewModel { SendViewModel(get(), WifiApi(get()), ActivityHelper()) }
    viewModel { MainViewModel(get()) }
}

object Module {
    const val PORT = "PORT"
}