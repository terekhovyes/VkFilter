package me.alexeyterekhov.vkfilter.NotificationService.CloudMessaging

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.Util.AppContext

object CloudMessagingLauncher {
    private val TAG = "CloudMessagingLauncher"

    // Launch methods
    fun isRunning(): Boolean {
        val manager = AppContext.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE))
            if (service.service.getClassName() == javaClass<CloudMessagingService>().getName()) {
                Log.d(TAG, "Service is already running")
                return true
            }
        Log.d(TAG, "Service isn't running")
        return false
    }
    fun launch() {
        val context = AppContext.instance
        context.startService(Intent(context, javaClass<CloudMessagingService>()))
    }
    fun kill() {
        val context = AppContext.instance
        context.stopService(Intent(context, javaClass<CloudMessagingService>()))
    }

    // Lifecycle methods
    fun onAuthorizedActivityOpened() {
        if (!isRunning() && notificationsEnabled())
            launch()
    }
    fun onLogout() {
        if (isRunning())
            kill()
    }
    fun checkServiceState() {
        val running = isRunning()
        when {
            notificationsEnabled() && !running -> launch()
            !notificationsEnabled() && running -> kill()
        }
    }

    private fun notificationsEnabled(): Boolean {
        val s = PreferenceManager.getDefaultSharedPreferences(AppContext.instance)
        return Settings.notificationsEnabled(s)
    }
}