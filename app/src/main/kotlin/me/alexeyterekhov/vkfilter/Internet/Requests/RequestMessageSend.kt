package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import org.json.JSONObject

class RequestMessageSend(
        val messages: Message,
        val dialogId: String,
        val isChat: Boolean
) : Request("messages.send") {
    init {
        params["message"] = messages.text
        params[if (isChat) "chat_id" else "user_id"] = dialogId
        val guid = System.currentTimeMillis()
        params["guid"] = System.currentTimeMillis()
        messages.sentId = guid

        params["attachment"] = AttachedCache.get(dialogId, isChat).generateAttachmentsParam()
    }

    override fun handleResponse(json: JSONObject) {
        val sentId = json.getLong("response")
        val guid = getSendingGuid()
        MessageCaches.getCache(dialogId, isChat).onDidSendMessage(guid, sentId)
    }

    fun getSendingGuid() = params["guid"] as Long
}