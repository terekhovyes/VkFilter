package me.alexeyterekhov.vkfilter.InternetOld.VkApi

import android.util.Log
import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import me.alexeyterekhov.vkfilter.InternetOld.ResponseHandler
import me.alexeyterekhov.vkfilter.Util.Chef
import me.alexeyterekhov.vkfilter.Util.Recipe

public object VkRecipes {
    private val MAX_WAIT_FOR_RESPONSE = 5000
    val LOG_TAG = "VkRequest"

    public val stoppableRecipe: Recipe<VkRequestBundle, VKResponse> = requestBase()
            .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
            .ifCookingFail(Chef.COOK_AGAIN_LATER)
            .waitAfterCookingFail(500)
            .create()

    public val unstoppabeRecipe: Recipe<VkRequestBundle, VKResponse> = requestBase()
            .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
            .ifCookingFail(Chef.COOK_AGAIN_LATER)
            .waitAfterCookingFail(500)
            .create()

    public val orderImportantRecipe: Recipe<VkRequestBundle, VKResponse> = requestBase()
            .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
            .ifCookingFail(Chef.COOK_AGAIN_IMMEDIATELY)
            .waitAfterCookingFail(500)
            .create()

    private fun requestBase() = Chef.createRecipe<VkRequestBundle, VKResponse>()
            .cookThisWay(cook())
            .serveThisWay(serve())
            .cleanUpThisWay(cleanUp())

    private fun cook(): (VkRequestBundle) -> VKResponse = {
        bundle: VkRequestBundle ->
        var gotResponse = false
        var gotError = false
        var response: VKResponse? = null

        val listener = object : VKRequest.VKRequestListener() {
            var canceled = false
            override fun onComplete(resp: VKResponse?) {
                if (!canceled) {
                    response = resp
                    gotResponse = true
                }
            }
            override fun onError(err: VKError?) {
                if (!canceled) {
                    super<VKRequest.VKRequestListener>.onError(err)
                    if (err != null && err.apiError != null)
                        Log.d(LOG_TAG, "Vk error: ${err.apiError.toString()}")
                    else
                        Log.d(LOG_TAG, "Vk error")
                    gotError = true
                }
            }
            fun cancel() {canceled = true}
        }

        // Start request
        val request = VKRequest(VkFunNames.name(bundle.vkFun), bundle.vkParams)
        request.attempts = 1
        request.executeWithListener(listener)
        Log.d(LOG_TAG, "Start [${VkFunNames.name(bundle.vkFun)}]")

        // Waiting while complete
        val sleepTime = 50L
        val maxCount = MAX_WAIT_FOR_RESPONSE / sleepTime
        var count = 0
        while (!gotResponse && !gotError && count < maxCount) {
            Thread.sleep(sleepTime)
            ++count
        }

        // If isn't done in 5 sec - try again
        if (!gotResponse && !gotError) {
            request.cancel()
            listener.cancel()
            throw Exception()
        }

        if (gotError || response == null || response!!.json == null) {
            throw Exception()
        } else response!!
    }

    private fun serve(): (VkRequestBundle, VKResponse) -> Unit = {
        request, response ->
        Log.d(LOG_TAG, "Done [${VkFunNames.name(request.vkFun)}]")
        ResponseHandler.handle(request, response.json)
    }

    private fun cleanUp(): (VkRequestBundle, Exception) -> Unit = {
        request, exception ->
        Log.d(LOG_TAG, "Error at [${VkFunNames.name(request.vkFun)}]")
    }
}