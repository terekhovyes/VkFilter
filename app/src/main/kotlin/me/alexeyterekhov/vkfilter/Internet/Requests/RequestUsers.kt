package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Internet.JSONParser
import org.json.JSONObject

class RequestUsers(val userIds: Collection<String>) : Request("users.get") {
    init {
        params["user_ids"] = userIds.join(separator = ",")
        params["fields"] = "name,sex,online,photo_max,last_seen"
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserList = json getJSONArray "response"
        JSONParser parseUsers jsonUserList forEach { UserCache putUser it }
        UserCache.dataUpdated()
    }
}