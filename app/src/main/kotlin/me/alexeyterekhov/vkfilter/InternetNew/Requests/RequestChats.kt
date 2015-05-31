package me.alexeyterekhov.vkfilter.InternetNew.Requests

import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.InternetNew.JSONParser
import org.json.JSONObject

class RequestChats(val chatIds: Collection<String>) : Request("execute.detailedChats") {
    init {
        params["chat_ids"] = chatIds.join(separator = ",")
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserList = json getJSONObject "response" getJSONArray "user_info"
        JSONParser parseUsers jsonUserList forEach { UserCache.putUser(it) }
        UserCache.dataUpdated()

        val jsonChatList = json getJSONObject "response" getJSONArray "chats"
        JSONParser parseChats jsonChatList forEach { ChatInfoCache.putChat(it.id.toString(), it) }
        ChatInfoCache.dataUpdated()
    }
}