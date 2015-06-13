package me.alexeyterekhov.vkfilter.Internet

import android.util.Log
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import me.alexeyterekhov.vkfilter.Util.Chef
import me.alexeyterekhov.vkfilter.Util.Recipe
import org.json.JSONObject

public object RequestControl {
    private val LOG_TAG = "RequestControl"

    fun addForeground(request: Request) = addRequest(request, RequestRecipes.foregroundRecipe)
    fun addBackground(request: Request) = addRequest(request, RequestRecipes.backgroundRecipe)
    fun addBackgroundOrdered(request: Request) = addRequest(request, RequestRecipes.backgroundOrderedRecipe)
    fun pause() = Chef.denyCooking(RequestRecipes.foregroundRecipe)
    fun resume() {
        checkSdkInitialized()
        Chef.allowCooking(RequestRecipes.foregroundRecipe)
    }

    private fun addRequest(request: Request, recipe: Recipe<Request, JSONObject>) {
        checkSdkInitialized()
        Log.d(LOG_TAG, ">>> Request [${request.getServerFunName()}]]")
        Chef.cook(recipe, request)
    }

    private fun checkSdkInitialized() {
        if (VkSdkInitializer.isNull())
            VkSdkInitializer.init()
    }
}