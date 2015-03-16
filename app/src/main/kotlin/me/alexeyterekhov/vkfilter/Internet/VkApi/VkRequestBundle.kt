package me.alexeyterekhov.vkfilter.Internet.VkApi

import com.vk.sdk.api.VKParameters
import android.os.Bundle

class VkRequestBundle (
        val vkFun: VkFun,
        val vkParams: VKParameters,
        val additionalParams: Bundle = Bundle()
)