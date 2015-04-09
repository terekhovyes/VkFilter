package me.alexeyterekhov.vkfilter.NotificationService


public trait NotificationListener {
    // Returns true, if completely handle notification
    fun onNotification(info: NotificationInfo): Boolean
}