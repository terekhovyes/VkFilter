package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import org.json.JSONObject

class RequestMessageSend(
        val message: Message,
        val dialogId: String,
        val isChat: Boolean
) : Request("messages.send") {
    init {
        params["message"] = message.text
        params[if (isChat) "chat_id" else "user_id"] = dialogId
        val guid = System.currentTimeMillis()
        params["guid"] = System.currentTimeMillis()
        message.sentId = guid

        // Add attachments
        params["attachment"] = AttachedCache.get(dialogId, isChat).generateAttachmentsParam()
        val images = AttachedCache.get(dialogId, isChat).images.getUploaded()
        AttachedCache.get(dialogId, isChat).images.removeUploaded()

        // Put attachment objects into message
        message.attachments.images addAll (images map { it.attachment })
    }

    override fun handleResponse(json: JSONObject) {
        val sentId = json.getLong("response")
        val guid = getSendingGuid()
        MessageCaches.getCache(dialogId, isChat).onDidSendMessage(guid, sentId)
    }

    fun getSendingGuid() = params["guid"] as Long
}