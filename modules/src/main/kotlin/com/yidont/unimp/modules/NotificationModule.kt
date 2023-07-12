package com.yidont.unimp.modules

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.yidont.unimp.modules.util.NoticeUtil
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.common.UniModule
import org.json.JSONObject


class NotificationModule : UniModule() {

    /**
     * {"title":"","text":""}
     */
    @UniJSMethod(uiThread = true)
    fun notify(data: JSONObject) {
        NoticeUtil.notify(mUniSDKInstance.context, data.getString("title"), data.getString("text"))
    }

    @UniJSMethod(uiThread = false)
    fun areNotificationsEnabled(): Boolean {
        val context = mUniSDKInstance.context ?: return false
        NoticeUtil.createNoticeChannel(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true
        if (!NoticeUtil.notificationManager(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (NoticeUtil.getPushMsgNoticeChannelImportance(context) < NotificationManager.IMPORTANCE_LOW) {
                return false
            }
        }
        return true
    }

    @UniJSMethod(uiThread = true)
    fun jumpNotificationSetting() {
        val context = mUniSDKInstance.context
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}