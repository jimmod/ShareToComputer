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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jim.sharetocomputer.databinding.FragmentMainBinding
import com.jim.sharetocomputer.logging.MyLog
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named


class MainFragment : Fragment() {

    private val port by inject<Int>(named("PORT"))
    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MyLog.i("onCreate")
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = mainViewModel
        mainViewModel.context = activity!!

        val request = arguments?.get(ARGS_REQUEST) as ShareRequest?
        mainViewModel.setRequest(request)
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