package me.alexeyterekhov.vkfilter.NotificationService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.alexeyterekhov.vkfilter.NotificationService.DataHandling.NotificationCollector

public class NotificationDismissBroadcast: BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        NotificationCollector.removeAllNotifications(c)
    }
}