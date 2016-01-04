package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import org.json.JSONObject

class RequestReadMessages(val dialogId: String, val isChat: Boolean) : Request("messages.markAsRead") {
    private var lastReadMessageId = 0L
    private var allowExecuteRequest = true

    init {
        val messages = MessageCaches.getCache(dialogId, isChat).getMessages()
        val notReadIncomes = messages.filter { it.isIn && it.isNotRead }
        if (notReadIncomes.isEmpty())
            allowExecuteRequest = false
        else {
            val notReadIds = notReadIncomes.map { it.sentId.toString() }
            params["message_ids"] = notReadIds.joinToString(separator = ",")
            lastReadMessageId = (notReadIncomes.maxBy { it.sentId })!!.sentId
        }
    }

    override fun allowExecuteRequest() = allowExecuteRequest

    override fun handleResponse(json: JSONObject) {
        val response = json.getInt("response")
        if (response == 1)
            MessageCaches
                    .getCache(dialogId, isChat)
                    .onReadMessages(out = false, lastId = lastReadMessageId)
    }
}