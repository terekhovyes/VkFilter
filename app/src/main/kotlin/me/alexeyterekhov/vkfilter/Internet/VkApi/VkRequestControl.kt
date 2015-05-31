package me.alexeyterekhov.vkfilter.Internet.VkApi

import android.util.Log
import me.alexeyterekhov.vkfilter.InternetNew.VkSdkInitializer
import me.alexeyterekhov.vkfilter.Util.Chef

public object VkRequestControl {
    private val LOG_TAG = "VkRequestControl"

    private fun checkSdkInitialized() {
        if (VkSdkInitializer.isNull())
            VkSdkInitializer.init()
    }

    public fun addStoppableRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        Log.d(LOG_TAG, ">>> Stoppable request [${VkFunNames.name(request.vkFun)}]]")
        Chef.cook(VkRecipes.stoppableRecipe, request)
    }
    public fun addUnstoppableRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        Log.d(LOG_TAG, ">>> Unstoppable request [${VkFunNames.name(request.vkFun)}]]")
        Chef.cook(VkRecipes.unstoppabeRecipe, request)
    }
    public fun addOrderImportantRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        Log.d(LOG_TAG, ">>> Order important request [${VkFunNames.name(request.vkFun)}]]")
        Chef.cook(VkRecipes.orderImportantRecipe, request)
    }

    public fun pause() {
        Chef.denyCooking(VkRecipes.stoppableRecipe)
    }

    public fun resume() {
        checkSdkInitialized()
        Chef.allowCooking(VkRecipes.stoppableRecipe)
    }
}