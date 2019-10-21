package com.jim.sharetocomputer.ext

import android.app.Instrumentation
import android.content.Intent
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import com.jim.sharetocomputer.logging.MyLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "startActivityForResult"

suspend fun FragmentActivity.startQrCodeScan(): Instrumentation.ActivityResult? {
    if (isFinishing) {
        return null
    }
    val result = CompletableDeferred<Instrumentation.ActivityResult>()
    val requestCode = IntentIntegrator.REQUEST_CODE

    var fragment = supportFragmentManager.findFragmentByTag(TAG) as FragmentHelper?
    if (fragment==null) {
        fragment = FragmentHelper()
        GlobalScope.launch(TestableDispatchers.Main) {
            MyLog.d("Add headless fragment")
            supportFragmentManager
                .beginTransaction()
                .add(fragment, TAG)
                .commitNowAllowingStateLoss()
        }
    }
    fragment.addMapping(requestCode, result)

    GlobalScope.launch(TestableDispatchers.Main) {
        IntentIntegrator.forSupportFragment(fragment).initiateScan()
    }
    return result.await()
}

class FragmentHelper : Fragment() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MyLog.i("onActivityResult $requestCode|$resultCode|${data?.extras?.keySet()}")
        map[requestCode].complete(Instrumentation.ActivityResult(resultCode, data))
        map.remove(requestCode)
    }

    fun addMapping(requestCode: Int, result: CompletableDeferred<Instrumentation.ActivityResult>) {
        MyLog.d("Add result code mapping $requestCode|")
        map.put(requestCode, result)
    }

    companion object {
        private val map = SparseArray<CompletableDeferred<Instrumentation.ActivityResult>>()
    }

}