package me.alexeyterekhov.vkfilter.InternetNew.Requests

import org.json.JSONObject

class RequestGCMRegister(val id: String) : Request("account.registerDevice") {
    init {
        params["token"] = id
        params["subscribe"] = "msg"
    }
    override fun handleResponse(json: JSONObject) {}
}