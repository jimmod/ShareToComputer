package com.jim.sharetocomputer

import android.app.Instrumentation
import android.content.Intent
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.jim.sharetocomputer.coroutines.TestableDispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

private const val TAG = "startActivityForResult"

suspend fun FragmentActivity.startActivityForResult(intent: Intent): Instrumentation.ActivityResult? {
    if (isFinishing) {
        return null
    }
    val result = CompletableDeferred<Instrumentation.ActivityResult>()
    val requestCode = Math.random().toInt()

    var fragment = supportFragmentManager.findFragmentByTag(TAG) as FragmentHelper?
    if (fragment==null) {
        fragment = FragmentHelper()
        GlobalScope.launch(TestableDispatchers.Main) {
            Timber.d("Add headless fragment")
            supportFragmentManager
                .beginTransaction()
                .add(fragment, TAG)
                .commitNowAllowingStateLoss()
        }
    }
    fragment.addMapping(requestCode, result)

    GlobalScope.launch(TestableDispatchers.Main) {
        fragment.startActivityForResult(intent, requestCode)
    }
    return result.await()
}

class FragmentHelper : Fragment() {

    private val map = SparseArray<CompletableDeferred<Instrumentation.ActivityResult>>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult $requestCode|$resultCode")
        map[requestCode].complete(Instrumentation.ActivityResult(resultCode, data))
    }

    fun addMapping(requestCode: Int, result: CompletableDeferred<Instrumentation.ActivityResult>) {
        Timber.d("Add result code mapping $requestCode|")
        map.put(requestCode, result)
    }

}