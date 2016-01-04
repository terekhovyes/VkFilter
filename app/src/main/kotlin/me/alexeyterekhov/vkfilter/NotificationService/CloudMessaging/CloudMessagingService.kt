package me.alexeyterekhov.vkfilter.NotificationService.CloudMessaging

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.vk.sdk.VKSdk
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestGCMRegister
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestGCMUnregister
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer
import me.alexeyterekhov.vkfilter.NotificationService.DataHandling.IntentHandler
import me.alexeyterekhov.vkfilter.Util.Chef
import me.alexeyterekhov.vkfilter.Util.GooglePlay


public class CloudMessagingService : Service() {
    private val TAG = "CloudMessagingService"
    private val broadcastReceiver: BroadcastReceiver by lazy { createBroadcastReceiver() }
    private var cloudMessagingToken = ""

    // Override methods
    override fun onBind(intent: Intent) = null
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "I'm created ^^")
        VkSdkInitializer.init()
        if (VKSdk.wakeUpSession(applicationContext) && VKSdk.isLoggedIn()) {
            subscribeForNotifications()
            registerBroadcast()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "I'm destroyed :(")
        unregisterBroadcast()
        unsubscribeFromNotifications()
    }

    // Subscribe and unsubscribe to GCM server
    private fun subscribeForNotifications() {
        if (GooglePlay.checkGooglePlayServicesWithoutError(this)) {
            Chef.express(
                    cooking = {
                        val cloudMessaging = GoogleCloudMessaging.getInstance(this)
                        cloudMessagingToken = cloudMessaging.register("419930423637")
                    },
                    serving = {
                        if (cloudMessagingToken != "")
                            RequestControl addBackground RequestGCMRegister(cloudMessagingToken)
                    }
            )
        } else {
            cloudMessagingToken = ""
            Log.d("Google Play Services", "No valid Google Play Services APK found.")
        }

    }
    private fun unsubscribeFromNotifications() {
        if (cloudMessagingToken != "")
            RequestControl addBackground RequestGCMUnregister(cloudMessagingToken)
    }

    // Register and unregister broadcast receiver
    private fun registerBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.google.android.c2dm.intent.RECEIVE")
        intentFilter.addCategory("vkfilter.gcm")
        registerReceiver(broadcastReceiver, intentFilter)
    }
    private fun unregisterBroadcast() = unregisterReceiver(broadcastReceiver)

    // Handle intents
    private fun onReceiveIntent(intent: Intent) {
        IntentHandler.onReceiveNewIntent(applicationContext, intent)
    }
    private fun createBroadcastReceiver() = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = onReceiveIntent(intent)
    }
}