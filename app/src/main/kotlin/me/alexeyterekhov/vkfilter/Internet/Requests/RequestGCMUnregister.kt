package me.alexeyterekhov.vkfilter.Internet.Requests

import org.json.JSONObject

class RequestGCMUnregister(id: String) : Request("account.unregisterDevice") {
    init {
        params["token"] = id
    }
    override fun handleResponse(json: JSONObject) {}
}