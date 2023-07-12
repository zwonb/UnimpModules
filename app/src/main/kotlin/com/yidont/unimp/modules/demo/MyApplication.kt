package com.yidont.unimp.modules.demo

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.taobao.weex.WXSDKEngine
import com.yidont.unimp.modules.AppModule
import com.yidont.unimp.modules.FileModule
import com.yidont.unimp.modules.NotificationModule
import com.yidont.unimp.modules.WiFiModule
import io.dcloud.common.util.RuningAcitvityUtil
import io.dcloud.feature.sdk.DCSDKInitConfig
import io.dcloud.feature.sdk.DCUniMPSDK

class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        initSdk()
    }

    fun initSdk() {
        val name = RuningAcitvityUtil.getAppName(baseContext)
        if (!name.contains("pushservice")) {
            initMP()
            if (!name.contains("unimp")) {
                // 请在此处初始化其他三方SDK
            }
        }
    }

    private fun initMP() {
        try {
            WXSDKEngine.registerModule("AppModule", AppModule::class.java)
            WXSDKEngine.registerModule("WiFiModule", WiFiModule::class.java)
            WXSDKEngine.registerModule("NotificationModule", NotificationModule::class.java)
//            WXSDKEngine.registerModule("CameraModule", CameraModule::class.java)
            WXSDKEngine.registerModule("FileModule", FileModule::class.java)
        } catch (e: Exception) {
            Log.e("zwonb", "注册 mp module 出错", e)
        }
        try {
//            WXSDKEngine.registerComponent("webViewComponent", WebViewComponent::class.java)
        } catch (e: Exception) {
//             Log.e("zwonb", "注册 mp component 出错", e)
        }
        val config = DCSDKInitConfig.Builder()
            .setCapsule(false)
            .setEnableBackground(false) // 后台运行
            .build()
        DCUniMPSDK.getInstance().initialize(this, config) {
            Log.e("zwonb", "unimp 初始化完成 $it")
        }
    }


}