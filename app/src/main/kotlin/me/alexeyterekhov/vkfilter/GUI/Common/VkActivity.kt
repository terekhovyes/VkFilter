package me.alexeyterekhov.vkfilter.GUI.Common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.vk.sdk.VKSdk
import me.alexeyterekhov.vkfilter.GUI.LoginActivity.LoginActivity
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestSetOnline
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer
import me.alexeyterekhov.vkfilter.NotificationService.CloudMessaging.CloudMessagingLauncher

public open class VkActivity: AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        VkSdkInitializer.init()
        if (!VKSdk.wakeUpSession(this))
            toLoginActivity()
        CloudMessagingLauncher.onAuthorizedActivityOpened()
        RequestControl.resume()
        if (!Settings.getGhostModeEnabled())
            RequestControl addForeground RequestSetOnline()
    }

    override fun onPause() {
        super.onPause()
        RequestControl.pause()
    }

    protected fun toLoginActivity() {
        CloudMessagingLauncher.onLogout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}