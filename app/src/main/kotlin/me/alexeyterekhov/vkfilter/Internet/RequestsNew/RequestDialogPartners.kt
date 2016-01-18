package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestDialogPartners(dialogId: Long, isChat: Boolean) : Request("execute.getDialogPartners") {
    init {
        params["id"] = dialogId
        params["chat"] = if (isChat) 1 else 0
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserArray = json.getJSONArray("response")
        val users = JsonParserNew.parseUsers(jsonUserArray)
        UpdateHandler.users.updateUsers(users)
    }
}