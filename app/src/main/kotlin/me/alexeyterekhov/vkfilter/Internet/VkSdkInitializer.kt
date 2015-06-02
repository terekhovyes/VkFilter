package me.alexeyterekhov.vkfilter.Internet

import android.app.AlertDialog
import com.vk.sdk.*
import com.vk.sdk.api.VKError
import com.vk.sdk.dialogs.VKCaptchaDialog

object VkSdkInitializer {
    val vkScopes = arrayOf(
            VKScope.FRIENDS,
            VKScope.MESSAGES,
            VKScope.PHOTOS,
            VKScope.VIDEO,
            VKScope.NOHTTPS
    )

    fun isNull() = VKSdk.instance() == null
    fun init() = VKSdk.initialize(createSdkListener(), "4464413")

    private fun createSdkListener() = object: VKSdkListener() {
        override fun onCaptchaError(e: VKError) = VKCaptchaDialog(e).show()
        override fun onTokenExpired(t: VKAccessToken) = VKSdk.authorize(vkScopes, true, false)
        override fun onAccessDenied(e: VKError) {
            if (e.errorCode != VKError.VK_CANCELED)
                AlertDialog.Builder(VKUIHelper.getTopActivity()).setMessage(e.toString()).show()
        }
    }
}