package me.alexeyterekhov.vkfilter.Internet.Requests

import org.json.JSONObject

class RequestMeTyping(
        val dialogId: String,
        val isChat: Boolean
) : Request("messages.setActivity") {
    init {
        params[if (isChat) "chat_id" else "user_id"] = dialogId
        params["type"] = "typing"
    }

    override fun handleResponse(json: JSONObject) {
    }
}