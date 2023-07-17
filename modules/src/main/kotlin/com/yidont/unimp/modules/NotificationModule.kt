package com.yidont.unimp.modules

import com.yidont.library.utils.notifyBase
import com.yidont.library.utils.startNotificationSetting
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.common.UniModule


open class NotificationModule : UniModule() {

//    private val notificationManager: NotificationManager
//        get() = mUniSDKInstance.context.getSystemService(NotificationManager::class.java)

    @UniJSMethod(uiThread = false)
    open fun areNotificationsEnabled(): Boolean {
        val context = mUniSDKInstance.context ?: return false
        return com.yidont.library.utils.areNotificationsEnabled(context)
    }

    /**
     * 8.0以上创建通知渠道
     */
    @UniJSMethod(uiThread = true)
    fun createNotificationChannel(channelId: String, name: String, sound: String = "") {
        val context = mUniSDKInstance.context
        com.yidont.library.utils.createNotificationChannel(
            context, channelId, name, sound
        )
    }

    /**
     * 8.0以上通知渠道id是否开启
     */
    @UniJSMethod(uiThread = false)
    fun areNotificationsChannelEnabled(channelId: String): Boolean {
        val context = mUniSDKInstance.context ?: return false
        return com.yidont.library.utils.areNotificationsChannelEnabled(context, channelId)
    }

    /**
     * 跳转到通知设置
     */
    @UniJSMethod(uiThread = true)
    fun jumpNotificationSetting() {
        val context = mUniSDKInstance.context
        startNotificationSetting(context)
    }

    /**
     * 发送一个通知
     */
    @UniJSMethod(uiThread = true)
    open fun notify(channelId: String, notifyId: Int, title: String?, text: String?) {
        val context = mUniSDKInstance.context
        notifyBase(context, channelId, notifyId, title, text)
    }

}