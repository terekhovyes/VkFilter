package me.alexeyterekhov.vkfilter.Internet.Requests

import org.json.JSONObject

class RequestSetOffline: Request("account.setOffline") {
    override fun handleResponse(json: JSONObject) {}
}