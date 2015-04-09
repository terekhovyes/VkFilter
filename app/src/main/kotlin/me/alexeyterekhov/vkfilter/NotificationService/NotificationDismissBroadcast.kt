package me.alexeyterekhov.vkfilter.NotificationService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

public class NotificationDismissBroadcast: BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        NotificationMaker.clearAllNotifications(c)
    }
}