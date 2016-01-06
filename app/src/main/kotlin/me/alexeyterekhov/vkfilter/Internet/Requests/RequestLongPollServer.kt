package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.Internet.JSONParser
import me.alexeyterekhov.vkfilter.Internet.LongPoll.LongPollControl
import org.json.JSONObject

class RequestLongPollServer() : Request("messages.getLongPollServer") {
    init {
        params["use_ssl"] = "1"
        params["need_pts"] = "0"
    }

    override fun handleResponse(json: JSONObject) {
        val response = json.getJSONObject("response")
        val longPollConfig = JSONParser.parseLongPollConfig(response)
        LongPollControl.configure(longPollConfig)
    }
}