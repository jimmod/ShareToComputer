/*
 *     This file is part of Share To Computer  Copyright (C) 2019  Jimmy <https://github.com/jimmod/ShareToComputer>.
 *
 *     Share To Computer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Share To Computer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Share To Computer.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.jim.sharetocomputer.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jim.sharetocomputer.ShareRequest
import com.jim.sharetocomputer.databinding.FragmentMainBinding
import com.jim.sharetocomputer.logging.MyLog
import org.koin.android.viewmodel.ext.android.viewModel

class MainFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MyLog.i("onCreate")
        val binding = FragmentMainBinding.inflate(inflater, container, false)

        val sectionsPagerAdapter =
            SectionsPagerAdapter(activity!!, activity!!.supportFragmentManager)
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)

        (arguments?.get(ARGS_REQUEST) as ShareRequest?)?.let {
            mainViewModel.setRequest(it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        MyLog.i("onDestroy")
        super.onDestroyView()
    }

    companion object {
        private const val ARGS_REQUEST = "request"

        fun createBundle(request: ShareRequest): Bundle {
            return Bundle().apply {
                putParcelable(ARGS_REQUEST, request)
            }
        }
    }
}
