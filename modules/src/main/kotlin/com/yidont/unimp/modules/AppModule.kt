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
import com.alibaba.fastjson.JSON
import com.yidont.library.utils.KeepAliveUtil
import com.yidont.library.utils.TTSBean
import com.yidont.library.utils.TTSMessengerService
import com.yidont.library.utils.autoStartSetting
import com.yidont.library.utils.batteryOptimization
import com.yidont.library.utils.isHonor
import com.yidont.library.utils.isHuawei
import com.yidont.library.utils.isLocationEnabled
import com.yidont.library.utils.isOPPO
import com.yidont.library.utils.isViVo
import com.yidont.library.utils.isXIAOMI
import com.yidont.library.utils.requestIgnoreBatteryOptimizations
import com.yidont.library.utils.startAppSetting
import com.yidont.library.utils.startLocationSetting
import com.yidont.library.utils.startSystemSetting
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.common.UniDestroyableModule
import io.dcloud.feature.uniapp.utils.UniLogUtils

open class AppModule : UniDestroyableModule() {

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

    private var ttsBean: TTSBean? = null // 第一次绑定服务播报的信息
    private var messenger: Messenger? = null
    private val ttsConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            messenger = Messenger(service)
            val bean = ttsBean
            ttsBean = null
            if (bean != null) ttsSendMessage(bean)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            UniLogUtils.d("ttsConnection断开连接 onServiceDisconnected")
        }
    }

    private fun bindTTSService(context: Context?) {
        val intent = Intent(context, TTSMessengerService::class.java)
        context?.bindService(intent, ttsConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindTTSService(context: Context?) {
        context?.unbindService(ttsConnection)
    }

    private fun ttsSendMessage(bean: TTSBean?) {
        bean ?: return
        val message = Message.obtain().apply {
            data.putString(TTSMessengerService.TEXT, bean.text)
            data.putFloat(TTSMessengerService.PERCENT, bean.percent)
            data.putBoolean(TTSMessengerService.FLUSH, bean.flush)
        }
        try {
            messenger?.send(message)
        } catch (e: Exception) {
            UniLogUtils.e("ttsSendMessage 服务不在线", e)
            ttsBean = bean
            bindTTSService(mUniSDKInstance.context)
        }
    }

    /**
     * {"text":"内容","percent":0.6,"flush":false}
     */
    @UniJSMethod(uiThread = true)
    fun ttsSpeak(json: String) {
        val data = JSON.parseObject(json)
        val bean = TTSBean(
            data.getString("text"),
            data.getFloat("percent") ?: -1f,
            data.getBooleanValue("flush")
        )
        if (messenger == null) {
            ttsBean = bean
            bindTTSService(mUniSDKInstance.context)
        } else {
            ttsSendMessage(bean)
        }
    }

    @UniJSMethod(uiThread = true)
    fun appKeepAlive() {
        KeepAliveUtil.init(mUniSDKInstance.context.applicationContext)
    }

    @UniJSMethod(uiThread = true)
    fun appKeepAliveRelease() {
        KeepAliveUtil.release()
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

    @CallSuper
    override fun destroy() {
        if (messenger != null) {
            messenger = null
            unbindTTSService(mUniSDKInstance.context)
        }
    }

}