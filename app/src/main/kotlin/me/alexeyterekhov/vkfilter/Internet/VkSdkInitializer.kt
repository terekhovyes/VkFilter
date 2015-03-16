package me.alexeyterekhov.vkfilter.Internet

import com.vk.sdk.VKSdk
import com.vk.sdk.VKSdkListener
import com.vk.sdk.api.VKError
import com.vk.sdk.dialogs.VKCaptchaDialog
import com.vk.sdk.VKAccessToken
import android.app.AlertDialog
import com.vk.sdk.VKUIHelper
import com.vk.sdk.VKScope


object VkSdkInitializer {
    val vkScopes = array(
            VKScope.FRIENDS,
            VKScope.MESSAGES,
            VKScope.PHOTOS,
            VKScope.NOHTTPS
    )

    fun isNull() = VKSdk.instance() == null
    fun init() = VKSdk.initialize(createSdkListener(), "4464413")

    private fun createSdkListener() = object: VKSdkListener() {
        override fun onCaptchaError(e: VKError) = VKCaptchaDialog(e).show()
        override fun onTokenExpired(t: VKAccessToken) = VKSdk.authorize(vkScopes, true, false)
        override fun onAccessDenied(e: VKError) { AlertDialog.Builder(VKUIHelper.getTopActivity())
                .setMessage(e.toString()).show() }
    }
}