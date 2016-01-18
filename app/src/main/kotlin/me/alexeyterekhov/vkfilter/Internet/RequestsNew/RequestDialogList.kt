package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestDialogList(val offset: Int, val count: Int) : Request("execute.detailedDialogs") {
    init {
        params["offset"] = offset
        params["count"] = count
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserList = json.getJSONObject("response").getJSONArray("user_info")
        val jsonDialogList = json.getJSONObject("response").getJSONArray("items")

        val users = JsonParserNew.parseUsers(jsonUserList)
        UpdateHandler.users.updateUsers(users, postEvent = false)

        val dialogs = JsonParserNew.parseDialogs(jsonDialogList)
        UpdateHandler.dialogs.updateDialogList(dialogs, offset)
        UpdateHandler.users.postUsersUpdated(users)
    }
}