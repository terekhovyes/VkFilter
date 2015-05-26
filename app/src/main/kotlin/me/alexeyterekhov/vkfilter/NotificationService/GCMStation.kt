package me.alexeyterekhov.vkfilter.NotificationService

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.Util.AppContext
import java.util.LinkedList
import kotlin.properties.Delegates


object GCMStation {
	private val notificationListeners = LinkedList<NotificationListener>()
    private val intentListeners = LinkedList<IntentListener>()
    private var context: Context by Delegates.notNull()

    fun onNewIntent(context: Context, intent: Intent) {
        this.context = context
        Log.d("GCMStation", "Receive new intent")
        if (intent.getStringExtra("collapse_key") == "msg") {
            if (intentListeners.isEmpty()) {
                val messageId = intent getStringExtra "msg_id"
                RunFun notificationInfo messageId
            } else {
                for (l in intentListeners)
                    l onGetIntent intent
            }
        }
    }

    fun onLoadNotification(info: NotificationInfo) {
        if (allowNotification(info)) {
            var handled = false
            if (notificationListeners.isNotEmpty())
                for (l in notificationListeners)
                    if (l onNotification info) {
                        handled = true
                        break
                    }
            if (!handled)
                NotificationMaker.addNotification(context, info)
        }
    }

    fun addRawIntentListener(l: IntentListener) {
        intentListeners add l
        if (intentListeners.size() == 1 && !isServiceRunning())
            launchService()
    }
    fun removeRawIntentListener(l: IntentListener) {
        if (intentListeners.size() == 1 && !notificationsEnabled())
            killService()
        intentListeners remove l
    }

    fun addNotificationListener(l: NotificationListener) {
        notificationListeners add l
        if (notificationListeners.size() == 1 && !isServiceRunning())
            launchService()
    }
    fun removeNotificationListener(l: NotificationListener) {
        if (notificationListeners.size() == 1 && !notificationsEnabled())
            killService()
        notificationListeners remove l
    }

    fun onAuthorizedActivityOpen() {
        if (!isServiceRunning() && notificationsEnabled())
            launchService()
    }
    fun onLogout() {
        if (isServiceRunning())
            killService()
    }

    fun launchService() {
        val context = AppContext.instance
        context.startService(Intent(context, javaClass<GCMService>()))
    }
    fun killService() {
        val context = AppContext.instance
        context.stopService(Intent(context, javaClass<GCMService>()))
    }
    fun isServiceRunning(): Boolean {
        val manager = AppContext.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE))
            if (service.service.getClassName() == javaClass<GCMService>().getName()) {
                Log.d("GCMStation", "GCMService already running")
                return true
            }
        Log.d("GCMStation", "GCMService isn't running")
        return false
    }

    private fun notificationsEnabled(): Boolean {
        val s = PreferenceManager.getDefaultSharedPreferences(AppContext.instance)
        return Settings.notificationsEnabled(s)
    }
    private fun allowNotification(info: NotificationInfo) = NotificationFiltrator.allowNotification(info)
}