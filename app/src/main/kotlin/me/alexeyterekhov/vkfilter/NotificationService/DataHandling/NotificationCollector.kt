package me.alexeyterekhov.vkfilter.NotificationService.DataHandling

import android.content.Context
import android.os.Handler
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestCheckMessages
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import java.util.LinkedList

object NotificationCollector {
    private val CHECK_DELAY = 60000L
    private val notifications = LinkedList<NotificationInfo>()
    private val handler = Handler()
    private var checkScheduled = false

    fun addNotification(context: Context, notification: NotificationInfo) {
        if (isCleverMode())
            CleverDelayer(context, notification, handler)
        else
            addStupidNotification(context, notification)
    }
    fun addStupidNotification(context: Context, notification: NotificationInfo) {
        val forReplace = notifications filter { it canBeReplacedBy notification }
        notifications removeAll forReplace
        notifications add notification
        onDataChanged(context)
    }
    fun removeNotification(context: Context, notification: NotificationInfo) {
        notifications remove notification
        onDataChanged(context)
    }
    fun removeNotification(context: Context, dialogId: String, isChat: Boolean) {
        if (isChat)
            notifications removeAll (notifications filter { it.chatId == dialogId })
        else
            notifications removeAll (notifications filter { it.chatId == "" && it.senderId == dialogId })
        onDataChanged(context)
    }
    fun removeAllNotifications(context: Context) {
        notifications.clear()
        onDataChanged(context)
    }

    fun onDataChanged(context: Context) {
        NotificationMaker.showNotificationFor(context, notifications)
        scheduleMessageCheck(context)
    }
    fun scheduleMessageCheck(context: Context) {
        if (isCleverMode() && notifications.isNotEmpty() && !checkScheduled) {
            checkScheduled = true
            handler.postDelayed({
                val messageIds = notifications map { it.messageSentId.toLong() }
                RequestControl addBackground RequestCheckMessages(messageIds, {
                    readIds ->
                    checkScheduled = false
                    if (readIds.isNotEmpty()) {
                        notifications removeAll (notifications filter { readIds contains it.messageSentId.toLong() })
                        onDataChanged(context)
                    } else
                        scheduleMessageCheck(context)
                })
            }, CHECK_DELAY)
        }
    }

    private fun isCleverMode() = Settings.getCleverNotificationsEnabled()
}