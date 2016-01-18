package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestChats(val chatIds: Collection<String>) : Request("execute.detailedChats") {
    init {
        params["chat_ids"] = chatIds.joinToString(separator = ",")
    }

    override fun handleResponse(json: JSONObject) {
        val jsonUserArray = json.getJSONObject("response").getJSONArray("user_info")
        val userList = JsonParserNew.parseUsers(jsonUserArray)
        UpdateHandler.users.updateUsers(userList)

        val jsonChatArray = json.getJSONObject("response").getJSONArray("chats")
        val chatInformationList = JsonParserNew.parseChats(jsonChatArray)
        UpdateHandler.dialogs.updateChats(chatInformationList)
    }
}