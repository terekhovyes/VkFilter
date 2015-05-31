package me.alexeyterekhov.vkfilter.InternetNew.Requests

import me.alexeyterekhov.vkfilter.DataCache.FriendsListCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.InternetNew.JSONParser
import org.json.JSONObject

class RequestFriendList(val offset: Int, val count: Int) : Request("friends.get") {
    init {
        params["count"] = count
        params["offset"] = offset
        params["order"] = "hints"
        params["fields"] = "name,sex,photo_max,last_seen"
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserList = json getJSONObject "response" getJSONArray "items"

        val friends = JSONParser parseUsers jsonUserList
        friends forEach { UserCache putUser it }
        if (offset == 0)
            FriendsListCache reloadList friends
        else
            FriendsListCache addItems friends
    }
}