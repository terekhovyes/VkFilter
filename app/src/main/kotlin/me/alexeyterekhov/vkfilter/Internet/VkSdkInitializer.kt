package me.alexeyterekhov.vkfilter.Internet

import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import me.alexeyterekhov.vkfilter.Util.AppContext

object VkSdkInitializer {
    val vkScopes = arrayOf(
            VKScope.FRIENDS,
            VKScope.MESSAGES,
            VKScope.PHOTOS,
            VKScope.VIDEO,
            VKScope.NOHTTPS
    )

    fun isNull() = false
    fun init() = VKSdk.initialize(AppContext.instance)
}