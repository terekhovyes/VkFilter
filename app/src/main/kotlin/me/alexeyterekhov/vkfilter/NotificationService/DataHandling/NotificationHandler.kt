package me.alexeyterekhov.vkfilter.NotificationService.DataHandling

import android.content.Context
import me.alexeyterekhov.vkfilter.NotificationService.CloudMessaging.CloudMessagingLauncher
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import me.alexeyterekhov.vkfilter.NotificationService.NotificationListener
import java.util.*

object NotificationHandler {
    private val notificationListeners = LinkedList<NotificationListener>()

    fun onReceiveNewNotification(context: Context, notification: NotificationInfo) {
        if (!NotificationFiltrator.allowNotification(notification))
            return

        var handledByListeners = false
        notificationListeners.forEach {
            handledByListeners = handledByListeners || it.onNotification(notification)
        }

        if (!handledByListeners)
            NotificationCollector.addNotification(context, notification)
    }

    fun addNotificationListener(l: NotificationListener) {
        notificationListeners.add(l)
        CloudMessagingLauncher.checkServiceState()
    }

    fun removeNotificationListener(l: NotificationListener) {
        notificationListeners.remove(l)
        CloudMessagingLauncher.checkServiceState()
    }
}