package me.alexeyterekhov.vkfilter.NotificationService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


public class StaticBroadcast : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Log.d("Static Broadcast", "Receive intent")
        if (!GCMStation.isServiceRunning()) {
            GCMStation.onNewIntent(c, i)
            GCMStation.launchService()
        }
    }
}