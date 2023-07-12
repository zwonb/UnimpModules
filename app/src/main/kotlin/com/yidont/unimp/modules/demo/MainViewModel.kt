package com.yidont.unimp.modules.demo

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.dcloud.feature.sdk.DCUniMPSDK
import io.dcloud.feature.unimp.config.UniMPOpenConfiguration
import io.dcloud.feature.unimp.config.UniMPReleaseConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File

const val APP_ID = "__UNI__5E0EA28"

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var state by mutableStateOf(MainState())
        private set

    /**
     * 1打开小程序，2关闭主界面，
     */
    var event = MutableStateFlow(0)
        private set

    init {
        val app = getApplication<MyApplication>()

        val file = File(app.externalCacheDir, "$APP_ID.wgt")
        releaseStartMP(file.path)
    }

    fun releaseStartMP(path: String) {
        val configuration = UniMPReleaseConfiguration()
        configuration.wgtPath = path
        DCUniMPSDK.getInstance().releaseWgtToRunPath(APP_ID, configuration) { code, pArgs ->
            Log.e("zwonb", "code=$code pArgs=$pArgs")
            if (code == 1) {
                // 打开小程序
                viewModelScope.launch {
                    event.emit(1)
                }
            } else {
                // 释放wgt失败
                state = state.copy(tipText = "解压失败，$pArgs")
            }
        }
    }

    fun openMP() {
        state = state.copy(loading = false)
        try {
            val configuration = UniMPOpenConfiguration()
            val uniMP = DCUniMPSDK.getInstance().openUniMP(getApplication(), APP_ID, configuration)

            viewModelScope.launch {
                // 关闭首页
                delay(800)
                event.emit(2)
            }
        } catch (t: Throwable) {
            Log.e("zwonb", "打开小程序失败", t)
            state = state.copy(tipText = "打开小程序失败，请重试")
        }
    }

}

data class MainState(
    val isAgreePrivacy: Boolean = false,
    val loading: Boolean = false,
    val loadText: String? = null,
    val tipText: String? = null,
)