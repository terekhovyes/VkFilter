package me.alexeyterekhov.vkfilter.NotificationService.DataHandling

import android.content.Context
import android.content.Intent
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestNotificationInfo
import me.alexeyterekhov.vkfilter.NotificationService.CloudMessaging.CloudMessagingLauncher
import me.alexeyterekhov.vkfilter.NotificationService.IntentListener
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import java.util.LinkedList
import kotlin.properties.Delegates

object IntentHandler {
    private val intentListeners = LinkedList<IntentListener>()
    private var context: Context by Delegates.notNull()
    private var allowLoadingNotifications = true

    fun onReceiveNewIntent(context: Context, intent: Intent) {
        this.context = context
        val collapseKey = intent.getStringExtra("collapse_key")
        if (collapseKey == "msg" || collapseKey == "vkmsg") {
            intentListeners forEach { it.onGetIntent(intent) }
            if (allowLoadingNotifications) {
                val messageId = intent getStringExtra "msg_id"
                RequestControl addBackground RequestNotificationInfo(messageId)
            }
        }
    }

    fun onLoadNotification(notification: NotificationInfo) {
        if (allowLoadingNotifications)
            NotificationHandler.onReceiveNewNotification(context, notification)
    }

    fun allowLoadingNotifications(allow: Boolean) {
        allowLoadingNotifications = allow
    }

    fun addIntentListener(l: IntentListener) {
        intentListeners add l
        CloudMessagingLauncher.checkServiceState()
    }

    fun removeIntentListener(l: IntentListener) {
        intentListeners remove l
        CloudMessagingLauncher.checkServiceState()
    }
}