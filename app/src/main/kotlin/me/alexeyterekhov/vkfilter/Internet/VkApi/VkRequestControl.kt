package me.alexeyterekhov.vkfilter.Internet.VkApi

import android.util.Log
import me.alexeyterekhov.vkfilter.Common.Chef
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer

public object VkRequestControl {
    private fun checkSdkInitialized() {
        if (VkSdkInitializer.isNull())
            VkSdkInitializer.init()
    }

    public fun addRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        Log.d(VkTask.LOG_TAG, ">>> Add request [${VkFunNames.name(request.vkFun)}]")
        Chef.cook(VkRecipes.normalRecipe, request)
    }

    public fun addUnstoppableRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        Log.d(VkTask.LOG_TAG, ">>> Add unstoppable request [${VkFunNames.name(request.vkFun)}]")
        Chef.cook(VkRecipes.veryImportantRecipe, request)
    }

    public fun pause() {
        Chef.denyCooking(VkRecipes.normalRecipe)
    }

    public fun resume() {
        checkSdkInitialized()
        Chef.allowCooking(VkRecipes.normalRecipe)
    }
}