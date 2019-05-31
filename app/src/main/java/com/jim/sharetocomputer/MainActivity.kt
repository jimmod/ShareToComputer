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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.jim.sharetocomputer.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.main_nav_fragment)

        val request: ShareRequest? = intent.generateShareRequest()

        val bundle = if (request != null) {
            MainFragment.createBundle(request)
        } else {
            Bundle()
        }
        navController.setGraph(R.navigation.nav_main, bundle)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    private fun Intent.generateShareRequest(): ShareRequest? {
        if (action== Intent.ACTION_SEND && type?.startsWith("text")==true) {
            return ShareRequest.ShareRequestText(getStringExtra(Intent.EXTRA_TEXT) ?: "")
        } else if (action== Intent.ACTION_SEND) {
            getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let {uri ->
                return ShareRequest.ShareRequestSingleFile(uri)
            }
        } else if (action== Intent.ACTION_SEND_MULTIPLE) {
            getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let {uris ->
                return ShareRequest.ShareRequestMultipleFile(uris)
            }
        } else {
            Timber.w("Unknown action: $action|$type")
        }
        return null
    }
}
