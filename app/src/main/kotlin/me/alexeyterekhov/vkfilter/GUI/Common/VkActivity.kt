package me.alexeyterekhov.vkfilter.GUI.Common

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.vk.sdk.VKSdk
import com.vk.sdk.VKUIHelper
import me.alexeyterekhov.vkfilter.GUI.LoginActivity.LoginActivity
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestSetOnline
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation

public open class VkActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VKUIHelper.onCreate(this)
    }

    override fun onResume() {
        super.onResume()
        VkSdkInitializer.init()
        if (!VKSdk.wakeUpSession(this))
            toLoginActivity()
        GCMStation.onAuthorizedActivityOpen()
        VKUIHelper.onResume(this)
        RequestControl.resume()
        if (!Settings.getGhostModeEnabled())
            RequestControl addForeground RequestSetOnline()
    }

    override fun onPause() {
        super.onPause()
        RequestControl.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        VKUIHelper.onDestroy(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data)
    }

    protected fun toLoginActivity() {
        GCMStation.onLogout()
        val intent = Intent(this, javaClass<LoginActivity>())
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}