package me.alexeyterekhov.vkfilter.NotificationService

import android.content.Intent

public interface IntentListener {
    fun onGetIntent(intent: Intent)
}