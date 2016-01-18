package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestFriendList(val offset: Int, val count: Int) : Request("friends.get") {
    init {
        params["count"] = count
        params["offset"] = offset
        params["order"] = "hints"
        params["fields"] = "name,sex,photo_max,last_seen"
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserArray = json.getJSONObject("response").getJSONArray("items")

        val friends = JsonParserNew.parseUsers(jsonUserArray)
        UpdateHandler.users.updateUsers(friends)
        UpdateHandler.users.updateFriendList(friends, offset)
    }
}