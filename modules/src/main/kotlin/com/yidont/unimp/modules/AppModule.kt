package com.yidont.unimp.modules

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import androidx.annotation.CallSuper
import com.yidont.unimp.modules.util.KeepAlive
import com.yidont.unimp.modules.util.TTSMessengerService
import com.yidont.unimp.modules.util.autoStartSetting
import com.yidont.unimp.modules.util.batteryOptimization
import com.yidont.unimp.modules.util.isHonor
import com.yidont.unimp.modules.util.isHuawei
import com.yidont.unimp.modules.util.isLocationEnabled
import com.yidont.unimp.modules.util.isOPPO
import com.yidont.unimp.modules.util.isViVo
import com.yidont.unimp.modules.util.isXIAOMI
import com.yidont.unimp.modules.util.logE
import com.yidont.unimp.modules.util.requestIgnoreBatteryOptimizations
import com.yidont.unimp.modules.util.startAppSetting
import com.yidont.unimp.modules.util.startLocationSetting
import com.yidont.unimp.modules.util.startSystemSetting
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.common.UniDestroyableModule

open class AppModule : UniDestroyableModule() {

    /**
     * 测试服：develop 正式服：release
     */
    @UniJSMethod(uiThread = false)
    open fun appFlavor(): String = "release"

    /**
     * 升级App
     */
    @UniJSMethod(uiThread = true)
    open fun updateApp() {
        val context = mUniSDKInstance.context
        val intent = Intent().apply {
            setClassName(context, "com.yidont.foot.bath.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    /**
     * 升级uni小程序
     */
    @UniJSMethod(uiThread = true)
    open fun updateMP(url: String) {
        val context = mUniSDKInstance.context
        val intent = Intent().apply {
            setClassName(context, "com.yidont.foot.bath.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("url", url)
        }
        context.startActivity(intent)
    }

    /**
     * 跳转App设置界面
     */
    @UniJSMethod(uiThread = true)
    fun jumpAppSetting() {
        startAppSetting(mUniSDKInstance.context.applicationContext)
    }

    /**
     * 跳转系统设置界面
     */
    @UniJSMethod(uiThread = true)
    fun jumpSystemSetting() {
        startSystemSetting(mUniSDKInstance.context.applicationContext)
    }

    /**
     * 定位服务是否开启
     */
    @UniJSMethod(uiThread = false)
    fun locationEnabled(): Boolean {
        return isLocationEnabled(mUniSDKInstance.context)
    }

    /**
     * 跳转定位服务设置界面
     */
    @UniJSMethod(uiThread = true)
    fun jumpLocationSetting() {
        startLocationSetting(mUniSDKInstance.context)
    }

    @UniJSMethod(uiThread = false)
    fun getSystemFontScale(): Float {
        return Resources.getSystem().configuration.fontScale
    }

    /**
     * tts
     */

    private var ttsMessage: String? = null // 第一次绑定服务播报的信息
    private var messenger: Messenger? = null
    private val ttsConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            messenger = Messenger(service)
            val json = ttsMessage
            ttsMessage = null
            if (json != null) ttsSendMessage(json)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            logE("ttsConnection断开连接 onServiceDisconnected")
        }
    }

    private fun bindTTSService(context: Context?) {
        val intent = Intent(context, TTSMessengerService::class.java)
        context?.bindService(intent, ttsConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindTTSService(context: Context?) {
        context?.unbindService(ttsConnection)
    }

    private fun ttsSendMessage(json: String?) {
        json ?: return
        val message = Message.obtain().apply {
            data.putString(TTSMessengerService.MESSAGE, json)
        }
        try {
            messenger?.send(message)
        } catch (e: Exception) {
            logE("ttsSendMessage 服务不在线", e)
            ttsMessage = json
            bindTTSService(mUniSDKInstance.context)
        }
    }

    /**
     * {"text":"内容","percent":0.6,"flush":false}
     */
    @UniJSMethod(uiThread = true)
    fun ttsSpeak(json: String) {
        if (messenger == null) {
            ttsMessage = json
            bindTTSService(mUniSDKInstance.context)
        } else {
            ttsSendMessage(json)
        }
    }

    @UniJSMethod(uiThread = true)
    fun appKeepAlive() {
        KeepAlive.init(mUniSDKInstance.context.applicationContext)
    }

    @UniJSMethod(uiThread = true)
    fun appKeepAliveRelease() {
        KeepAlive.release()
    }

    @UniJSMethod(uiThread = true)
    fun appBatteryOptimization() {
        batteryOptimization(mUniSDKInstance.context)
    }

    @UniJSMethod(uiThread = true)
    fun jumpKeepAliveSetting() {
        val context = mUniSDKInstance.context
        when {
            isHuawei() || isHonor() -> autoStartSetting(context.applicationContext)
            isViVo() -> startSystemSetting(context.applicationContext)
            isOPPO() || isXIAOMI() -> startAppSetting(context.applicationContext)
            else -> requestIgnoreBatteryOptimizations(context)
        }
    }

    override fun onActivityResume() {
        logE(mUniSDKInstance.bundleUrl)
        logE(mWXSDKInstance.uniPagePath)
        logE(mUniSDKInstance.bundleUrl?.substringAfter("/apps/")?.substringBefore('/'))
    }

    @CallSuper
    override fun destroy() {
        if (messenger != null) {
            messenger = null
            unbindTTSService(mUniSDKInstance.context)
        }
    }

}