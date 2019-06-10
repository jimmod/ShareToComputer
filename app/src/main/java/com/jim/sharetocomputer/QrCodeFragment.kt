package com.jim.sharetocomputer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.ext.startBarcodeScan
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class QrCodeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        GlobalScope.launch(TestableDispatchers.Default) {
            activity?.startBarcodeScan()?.let { result ->
                val resultBarcode: IntentResult? = IntentIntegrator.parseActivityResult(
                    IntentIntegrator.REQUEST_CODE,
                    result.resultCode,
                    result.resultData
                )
                Timber.d("Result: ${resultBarcode?.contents}")
                GlobalScope.launch(TestableDispatchers.Main) {
                    findNavController().popBackStack()
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}