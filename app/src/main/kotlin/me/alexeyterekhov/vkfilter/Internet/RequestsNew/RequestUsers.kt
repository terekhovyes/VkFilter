package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestUsers(val userIds: Collection<String>) : Request("users.get") {
    init {
        params["user_ids"] = userIds.joinToString(separator = ",")
        params["fields"] = "name,sex,online,photo_max,last_seen"
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserArray = json.getJSONArray("response")
        val users = JsonParserNew.parseUsers(jsonUserArray)
        UpdateHandler.users.updateUsers(users)
    }
}