package me.alexeyterekhov.vkfilter.InternetNew.Requests

import com.vk.sdk.api.VKParameters
import org.json.JSONObject

public abstract class Request(serverFun: String) {
    private val serverFunName = serverFun
    protected val params: VKParameters = VKParameters()

    fun getParameters() = params
    fun getServerFunName() = serverFunName
    abstract fun handleResponse(json: JSONObject)
}