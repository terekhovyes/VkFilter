package me.alexeyterekhov.vkfilter.NotificationService.DataHandling

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.support.v4.app.NotificationCompat
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.TextFormat

object NotificationMaker {
    private val NOTIFICATION_ID = 1

    fun showNotificationFor(context: Context, notifications: List<NotificationInfo>) {
        if (notifications.isEmpty())
            killNotification(context)
        else {
            val builder = if (notifications.count() == 1)
                createSingleNotification(notifications.first(), context)
            else
                createMultiNotification(notifications, context)
            builder.setNumber(notifications.count())

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun killNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }

    private fun createSingleNotification(n: NotificationInfo, context: Context): NotificationCompat.Builder {
        val onClickIntent = NotificationUtil.createChatActivityIntent(context, n)

        val fullTitle = n.getName(compact = false)
        val title = if (fullTitle.count() > 18)
            n.getName(compact = true)
        else
            fullTitle

        val bigTextStyle = NotificationCompat
                .BigTextStyle()
                .bigText(n.messageText)

        return createNotificationBase(context, n.senderPhotoUrl)
                .setContentTitle(title)
                .setContentText(n.messageText)
                .setContentIntent(onClickIntent)
                .setStyle(bigTextStyle)
    }

    private fun createMultiNotification(notifications: List<NotificationInfo>, context: Context): NotificationCompat.Builder {
        val firstDialog = notifications.last()
        val moreDialogs = notifications.reverse() drop 1 take 3
        val notShownCount = notifications.count() - moreDialogs.count() - 1
        val onClickIntent = NotificationUtil.createDialogsActivityIntent(context)

        val inboxStyle = NotificationCompat.InboxStyle()
        moreDialogs forEach { n ->
            inboxStyle addLine "${n.getName(compact = false)}: ${n.messageText}"
        }
        if (notShownCount > 0)
            inboxStyle setSummaryText TextFormat.andMoreDialogs(context, notShownCount)
        inboxStyle setBigContentTitle "${firstDialog.getName(compact = true)}: ${firstDialog.messageText}"

        return createNotificationBase(context, firstDialog.senderPhotoUrl)
                .setContentTitle("${firstDialog.getName()}")
                .setContentText(firstDialog.messageText)
                .setContentIntent(onClickIntent)
                .setStyle(inboxStyle)
    }

    private fun createNotificationBase(context: Context, photoUrl: String): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context)
                .setColor(context.getResources().getColor(R.color.m_green))
                .setSmallIcon(R.drawable.icon_notification)
                .setDeleteIntent(NotificationUtil.createDismissIntent(context))
        val photo = NotificationUtil.loadPhoto(context, photoUrl)
        if (photo != null)
            builder.setLargeIcon(photo)
        if (NotificationUtil.vibrationAllowed(context)) {
            val arr = LongArray(4)
            arr.set(0, 0)
            arr.set(1, 300)
            arr.set(2, 300)
            arr.set(3, 300)
            builder.setVibrate(arr)
        }
        if (NotificationUtil.soundAllowed(context)) {
            val soundUri = Uri.parse("android.resource://${context.getPackageName()}/${R.raw.message_sound}")
            builder.setSound(soundUri)
        }
        if (NotificationUtil.colorLightAllowed(context))
            builder.setLights(0xFF00FF91.toInt(), 1000, 5000)
        else
            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
        return builder
    }
}