package me.alexeyterekhov.vkfilter.NotificationService

import android.content.Intent

public trait IntentListener {
    fun onGetIntent(intent: Intent)
}