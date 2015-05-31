package me.alexeyterekhov.vkfilter.InternetNew.Requests

import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.InternetNew.JSONParser
import org.json.JSONObject

class RequestDialogPartners(dialogId: Long, isChat: Boolean) : Request("execute.getDialogPartners") {
    init {
        params["id"] = dialogId
        params["chat"] = if (isChat) 1 else 0
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserList = json getJSONArray "response"
        JSONParser parseUsers jsonUserList forEach { UserCache putUser it }
        UserCache.dataUpdated()
    }
}