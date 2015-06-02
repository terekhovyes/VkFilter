package me.alexeyterekhov.vkfilter.NotificationService


public interface NotificationListener {
    // Returns true, if completely handle notification
    // Returns false, if notification still has to be handled
    fun onNotification(info: NotificationInfo): Boolean
}