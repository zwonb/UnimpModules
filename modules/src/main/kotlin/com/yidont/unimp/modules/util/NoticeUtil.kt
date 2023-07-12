package com.yidont.unimp.modules.util

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.yidont.unimp.modules.R

/**
 * @author zwonb
 * @date 2020/9/14
 */
object NoticeUtil {

    val PUSH_SERVICE_ID get() = "pushService"
    val PUSH_SERVICE_NAME get() = "前台服务"

    private val PUSH_NOTICE_ID get() = "pushNotice"
    private val PUSH_NOTICE_NAME get() = "服务提醒"

    val PUSH_SERVICE_NOTICE_ID get() = 1

    val ttsId get() = 2
    val noticeId get() = 3

    @TargetApi(Build.VERSION_CODES.O)
    fun createNoticeChannel(context: Context) {
        createNotificationChannel(
            context, PUSH_NOTICE_ID, PUSH_NOTICE_NAME, NotificationManager.IMPORTANCE_HIGH
        )
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        context: Context,
        channelId: String?,
        channelName: String?,
        importance: Int,
        sound: String = "",
        groupId: String = "",
    ) {
        val manager = notificationManager(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                if (groupId.isNotEmpty()) {
                    group = groupId
                }
                if (sound.isNotEmpty()) {
                    val uri = "android.resource://${context.packageName}/raw/$sound"
                    val attributes =
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
                    setSound(Uri.parse(uri), attributes)
                }
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun notificationManager(context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun getPushServiceNotice(context: Context): NotificationCompat.Builder {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, flag)
        return NotificationCompat.Builder(context, PUSH_SERVICE_ID).setShowWhen(false)
            .setContentTitle(null)
            .setContentText("服务提醒系统运行中")
            .setContentIntent(pendingIntent)
//            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.push))
            .setSmallIcon(R.drawable.push_small)
//                .setColor(context.getColor(R.color.theme))
    }

    fun getPushMsgNotice(context: Context): NotificationCompat.Builder {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, flag)
        return NotificationCompat.Builder(context, PUSH_NOTICE_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE).setAutoCancel(true)
            .setContentTitle("消息通知")
            .setContentIntent(pendingIntent)
            .setLargeIcon(null)
            .setSmallIcon(R.drawable.push_small)
//                .setColor(context.getColor(R.color.theme))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPushMsgNoticeChannel(context: Context): NotificationChannel? {
        return notificationManager(context).getNotificationChannel(PUSH_NOTICE_ID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPushMsgNoticeChannelImportance(context: Context): Int {
        return getPushMsgNoticeChannel(context)?.importance ?: 0
    }

    fun notify(context: Context, title: String? = null, text: String?) {
        val style = NotificationCompat.BigTextStyle()
        style.bigText(text)
        val msgNotice = getPushMsgNotice(context)
            .setContentTitle(title ?: "消息通知")
            .setContentText(text)
            .setStyle(style)
            .build()
        notificationManager(context).notify(noticeId, msgNotice)
    }

}

//fun getNoticeTip(): String = when {
//    isHuawei() || isHonor() -> "1、请找到[通知管理]\n2、开启[允许通知]\n3、设置所有通知类型\n4、关闭[静默通知]\n5、允许打扰"
//    isViVo() -> "1、开启[允许通知]\n2、关闭[接受智能通知控制]\n3、进入[类别-服务提醒];开启[允许通知];关闭[接受智能通知控制];重要程度选择[优先显示]/[紧急];开启[勿扰时允许提醒]"
//    isOPPO() || isOnePlus() -> "1、开启[允许通知]\n2、勾选[锁屏]、[横幅]\n3、进入[类别-服务提醒];开启[允许通知];关闭[设为不重要通知];开启[在状态栏显示];开启[震动];开启[免打扰下允许通知提醒]"
//    isXIAOMI() -> "1、开启[允许通知]\n2、进入[通知过滤规则]-选择[全部设为重要]；开启[悬浮通知权限]；开启[锁屏通知权限]；开启[震动权限]\n3、进入[通知类别-服务提醒]；开启[允许通知]；开启[悬浮通知权限]；开启[震动]；进入[在锁定屏幕上]-选择[显示通知及其内容]"
//    else -> "请前往设置开启[服务提醒]通知"
//}

//fun keepAliveTip(): String = when {
//    isHuawei() || isHonor() -> "为了保证服务提醒稳定运行，需设置允许App后台运行，并锁定App，关闭省电模式\n1、点击设置\n2、在[应用启动管理]找到[调理师叮客云智慧足道]\n3、关闭[自动管理]，弹窗后开启[允许自启动]、[允许关联启动]、[允许后台活动]，确定即可\n4、切到多任务窗口，滑动到调理师App，往下拉锁定App"
//    isViVo() -> "为了保证服务提醒稳定运行，需设置允许App后台运行，并锁定App，关闭省电模式\n1、点击设置\n2、找到并进入[电池]\n3、进入[后台(高)耗电]\n4、找到[调理师叮客云智慧足道]进入选择[允许后台高耗电]或打开开关；返回到系统设置找到[应用与权限]进入，点击[权限管理]、切换到[权限]、点击[自启动]、找到[调理师叮客云智慧足道]开启[后台启动]开启[关联启动]，如果关联启动没有出现[调理师叮客云智慧足道]开关，需要关闭App，重新打开App后立马关闭App，等10秒钟之后再次查看\n5、切到多任务窗口，滑动到调理师App，点击右上角图标锁定App"
//    isOPPO() || isOnePlus() -> "为了保证服务提醒稳定运行，需设置允许App后台运行，并锁定App，关闭省电模式\n1、点击设置进入应用详情\n2、进入[耗电管理]\n3、开启[允许完全后台行为]、开启[允许应用自启动]、开启[允许应用关联启动]。打开手机[设置-电池-更多(设置)-耗电异常优化]，找到[调理师叮客云智慧足道]点击选择[不优化]\n4、切到多任务窗口，滑动到调理师App，点击右上角图标锁定App"
//    isXIAOMI() -> "为了保证服务提醒稳定运行，需设置允许App后台运行，并锁定App，关闭省电模式\n1、点击设置进入应用详情\n2、进入[省电策略]选择[无限制]\n3、开启[自启动]\n4、切到多任务窗口，滑动到调理师App，长按锁定App"
//    else -> "为了保证服务提醒稳定运行，需设置允许App后台运行，并锁定App，关闭省电模式"
//}