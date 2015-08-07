package me.alexeyterekhov.vkfilter.NotificationService

import android.content.Intent

interface IntentListener {
    fun onGetIntent(intent: Intent)
}