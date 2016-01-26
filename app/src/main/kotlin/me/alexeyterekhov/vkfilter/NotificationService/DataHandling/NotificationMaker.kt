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

        return createNotificationBase(context, n.senderPhotoUrl, 1)
                .setContentTitle(title)
                .setContentText(n.messageText)
                .setContentIntent(onClickIntent)
                .setStyle(bigTextStyle)
    }

    private fun createMultiNotification(notifications: List<NotificationInfo>, context: Context): NotificationCompat.Builder {
        val firstDialog = notifications.last()
        val moreDialogs = notifications.reversed().drop(1).take(3)
        val notShownCount = notifications.count() - moreDialogs.count() - 1
        val onClickIntent = NotificationUtil.createDialogsActivityIntent(context)

        val inboxStyle = NotificationCompat.InboxStyle()
        moreDialogs.forEach { n ->
            inboxStyle.addLine("${n.getName(compact = false)}: ${n.messageText}")
        }
        if (notShownCount > 0)
            inboxStyle.setSummaryText(TextFormat.andMoreDialogs(context, notShownCount))
        inboxStyle.setBigContentTitle("${firstDialog.getName(compact = true)}: ${firstDialog.messageText}")

        return createNotificationBase(context, firstDialog.senderPhotoUrl, notifications.count())
                .setContentTitle("${firstDialog.getName()}")
                .setContentText(firstDialog.messageText)
                .setContentIntent(onClickIntent)
                .setStyle(inboxStyle)
    }

    private fun createNotificationBase(context: Context, photoUrl: String, messageCount: Int): NotificationCompat.Builder {
        val iconRes = iconResForCount(context, messageCount)
        val builder = NotificationCompat.Builder(context)
                .setColor(context.resources.getColor(R.color.m_green))
                .setSmallIcon(iconRes)
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
            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.message_sound}")
            builder.setSound(soundUri)
        }
        if (NotificationUtil.colorLightAllowed(context))
            builder.setLights(0xFF00FF91.toInt(), 1000, 5000)
        else
            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
        return builder
    }

    private fun iconResForCount(context: Context, count: Int): Int {
        return when {
            !NotificationUtil.counterAllowed(context) -> R.drawable.icon_notification
            count == 1 -> R.drawable.icon_notification_1
            count == 2 -> R.drawable.icon_notification_2
            count == 3 -> R.drawable.icon_notification_3
            count == 4 -> R.drawable.icon_notification_4
            count == 5 -> R.drawable.icon_notification_5
            count == 6 -> R.drawable.icon_notification_6
            count == 7 -> R.drawable.icon_notification_7
            count == 8 -> R.drawable.icon_notification_8
            count == 9 -> R.drawable.icon_notification_9
            count > 9 -> R.drawable.icon_notification_many
            else -> R.drawable.icon_notification
        }
    }
}