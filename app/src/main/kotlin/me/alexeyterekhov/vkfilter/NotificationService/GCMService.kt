package me.alexeyterekhov.vkfilter.NotificationService

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.vk.sdk.VKSdk
import com.vk.sdk.VKUIHelper
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestGCMRegister
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestGCMUnregister
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer
import me.alexeyterekhov.vkfilter.Util.GooglePlay
import java.io.IOException
import kotlin.properties.Delegates


public class GCMService: Service() {
    private val GCMReceiver: BroadcastReceiver by Delegates.lazy { createReceiver() }
    private var registeredToken = ""

    override fun onBind(intent: Intent) = null
    override fun onCreate() {
        super.onCreate()
        Log.d("GCMService", "I'm created ^^")
        VKUIHelper.setApplicationContext(getApplicationContext())
        VkSdkInitializer.init()
        if (VKSdk.wakeUpSession(getApplicationContext()) && VKSdk.isLoggedIn()) {
            registerGCM()
            enableReceiver()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("GCMService", "I'm destroyed :(")
        disableReceiver()
        unregisterGCM(registeredToken)
    }

    private fun registerGCM() {
        if (GooglePlay.checkGooglePlayServicesWithoutError(this)) {
            (object: AsyncTask<Unit, Unit, Unit>() {
                var registeredKey = ""
                override fun doInBackground(vararg params: Unit?) {
                    try {
                        val gcm = GoogleCloudMessaging.getInstance(this@GCMService)
                        registeredKey = gcm register "419930423637"
                    } catch (e: IOException) { e.printStackTrace() }
                }
                override fun onPostExecute(result: Unit?) {
                    super.onPostExecute(result)
                    registeredToken = registeredKey
                    if (registeredToken != "")
                        RequestControl addBackground RequestGCMRegister(registeredToken)
                }
            }).execute()
        } else {
            registeredToken = ""
            Log.d("Google Play Services", "No valid Google Play Services APK found.")
        }
    }
    private fun unregisterGCM(id: String) {
        if (id != "")
            RequestControl addBackground RequestGCMUnregister(id)
    }
    private fun enableReceiver() {
        val filter = IntentFilter()
        filter addAction "com.google.android.c2dm.intent.RECEIVE"
        filter addCategory "vkfilter.gcm"
        registerReceiver(GCMReceiver, filter)
    }
    private fun disableReceiver() = unregisterReceiver(GCMReceiver)

    private fun onNewIntent(intent: Intent) {
        GCMStation.onNewIntent(getApplicationContext(), intent)
    }

    private fun createReceiver() = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent)
            = onNewIntent(intent)
    }
}