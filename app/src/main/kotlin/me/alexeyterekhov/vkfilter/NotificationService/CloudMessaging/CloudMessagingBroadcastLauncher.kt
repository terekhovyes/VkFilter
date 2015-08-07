package me.alexeyterekhov.vkfilter.NotificationService.CloudMessaging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import me.alexeyterekhov.vkfilter.NotificationService.DataHandling.IntentHandler

public class CloudMessagingBroadcastLauncher : BroadcastReceiver() {
    private val TAG = "CloudMessagingBroadcast"

    override fun onReceive(c: Context, i: Intent) {
        Log.d(TAG, "Receive intent")
        if (CloudMessagingLauncher.isRunning()) {
            IntentHandler.onReceiveNewIntent(c, i)
            CloudMessagingLauncher.launch()
        }
    }
}