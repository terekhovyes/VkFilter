package me.alexeyterekhov.vkfilter.Internet.Requests

import org.json.JSONObject

class RequestSetOnline: Request("account.setOnline") {
    override fun handleResponse(json: JSONObject) {}
}