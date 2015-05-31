package me.alexeyterekhov.vkfilter.Internet.Requests

import android.os.Handler
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import org.json.JSONObject

class RequestReadMessages(val dialogId: String, val isChat: Boolean) : Request("messages.markAsRead") {
    private var lastReadMessageId = 0L

    init {
        val messages = MessageCaches.getCache(dialogId, isChat).getMessages()
        val notRead = messages filter { it.isIn && it.isNotRead }
        if (notRead.isNotEmpty()) {
            val ids = notRead map { it.sentId.toString() }
            params["message_ids"] = ids.joinToString(separator = ",")
            lastReadMessageId = (notRead maxBy { it.sentId })!!.sentId
        }
    }

    override fun handleResponse(json: JSONObject) {
        val response = json getInt "response"
        if (response == 1)
            MessageCaches.getCache(dialogId, isChat).onReadMessages(out = false, lastId = lastReadMessageId)
        else
            Handler().postDelayed({
                RequestControl addForeground RequestReadMessages(dialogId, isChat)
            }, 1000)
    }
}