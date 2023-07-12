package com.yidont.unimp.modules.util

import android.util.Log
import com.yidont.unimp.modules.BuildConfig

fun logD(msg: String?) {
    if (BuildConfig.DEBUG) {
        Log.d("zwonb", msg ?: "")
    }
}

fun logE(msg: String?, t: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.e("zwonb", msg, t)
    }
}