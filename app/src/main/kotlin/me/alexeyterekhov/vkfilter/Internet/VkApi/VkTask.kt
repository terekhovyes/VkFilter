package me.alexeyterekhov.vkfilter.Internet.VkApi

import com.vk.sdk.api.VKResponse
import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKRequest
import android.util.Log
import com.vk.sdk.api.VKRequest.VKRequestListener
import kotlin.properties.Delegates
import me.alexeyterekhov.backgroundtask.BackgroundTask
import me.alexeyterekhov.backgroundtask.SourceHandler
import me.alexeyterekhov.backgroundtask.ErrorHandler
import me.alexeyterekhov.backgroundtask.ResultHandler
import me.alexeyterekhov.vkfilter.Internet.ResponseHandler


object VkTask {
    private val MAX_WAIT_FOR_RESPONSE = 5000
    private val LOG_TAG = "VKBackgroundTask"

    public val instance: BackgroundTask<VkRequestBundle, VKResponse> by Delegates.lazy {
        var obj = BackgroundTask (
                createSourceHandler(),
                createResultHandler(),
                createErrorHandler()
        )
        with (obj) {
            setMaxAttempts(0)
            setRepeatOnFailMode(BackgroundTask.REPEAT_ALL_FAILED)
            setDelayBetweenAttempts(500)
        }
        obj
    }
    public val unstoppableInstance: BackgroundTask<VkRequestBundle, VKResponse> by Delegates.lazy {
        var obj = BackgroundTask (
                createSourceHandler(),
                createResultHandler(),
                createErrorHandler()
        )
        with (obj) {
            setMaxAttempts(0)
            setRepeatOnFailMode(BackgroundTask.REPEAT_ONE_WHILE_FAIL)
            setDelayBetweenAttempts(500)
        }
        obj
    }

    private fun createSourceHandler() = object : SourceHandler<VkRequestBundle, VKResponse> {
        override fun getResult(
                task: BackgroundTask<out Any?, out Any?>?,
                bundle: VkRequestBundle
        ): VKResponse? {
            var gotResponse = false
            var gotError = false
            var response: VKResponse? = null
            val listener = object : VKRequestListener() {
                var canceled = false
                override fun onComplete(resp: VKResponse?) {
                    if (!canceled) {
                        response = resp
                        gotResponse = true
                    }
                }
                override fun onError(err: VKError?) {
                    if (!canceled) {
                        super<VKRequestListener>.onError(err)
                        Log.d("Error", "Vk error while do request")
                        gotError = true
                    }
                }
                fun cancel() {canceled = true}
            }

            // Start request
            val request = VKRequest(VkFunNames.name(bundle.vkFun), bundle.vkParams)
            request.attempts = 1
            request.executeWithListener(listener)
            Log.d(LOG_TAG, "Do request for vk: ${VkFunNames.name(bundle.vkFun)}")

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
            } else return response
        }
    }

    private fun createErrorHandler() = object : ErrorHandler<VkRequestBundle> {
        override fun onError(
                task: BackgroundTask<out Any?, out Any?>?,
                bundle: VkRequestBundle?,
                e: Exception?)
        {
            Log.d(LOG_TAG, "error, will try again")
        }
    }

    private fun createResultHandler() = object : ResultHandler<VkRequestBundle, VKResponse> {
        override fun onNext(task: BackgroundTask<out Any?, out Any?>?,
                            bundle: VkRequestBundle,
                            result: VKResponse) {
            Log.d(LOG_TAG, "ok!")
            ResponseHandler.handle(bundle, result.json)
        }
        override fun onComplete(task: BackgroundTask<out Any?, out Any?>?) {}
    }
}