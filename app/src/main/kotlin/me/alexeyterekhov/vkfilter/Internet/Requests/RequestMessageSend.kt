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

        // Attachments and forward messages
        val attached = AttachedCache.get(dialogId, isChat)
        val attachmentValue = attached.generateAttachmentsParam()
        val messagesValue = attached.generateForwardMessagesParam()
        if (attachmentValue.isNotBlank()) {
            params["attachment"] = attachmentValue
            val images = attached.images.getUploaded()
            attached.images.removeUploaded()
            message.attachments.images.addAll(images.map { it.attachment })
        }
        if (messagesValue.isNotBlank()) {
            params["forward_messages"] = messagesValue
            val attachedMessages = attached.messages.get().first()
            attached.messages.clear()
            val dialog = MessageCaches.getCache(attachedMessages.dialogId, attachedMessages.isChat).getMessages()
            attachedMessages.messageIds.forEach { id ->
                message.attachments.messages.add(dialog.firstOrNull { it.sentId == id })
            }
        }
    }

    override fun handleResponse(json: JSONObject) {
        val sentId = json.getLong("response")
        val guid = getSendingGuid()
        MessageCaches.getCache(dialogId, isChat).onDidSendMessage(guid, sentId)
    }

    fun getSendingGuid() = params["guid"] as Long
}