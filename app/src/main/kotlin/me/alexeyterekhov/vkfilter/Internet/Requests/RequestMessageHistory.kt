package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.JSONParser
import me.alexeyterekhov.vkfilter.Internet.MissingDataAnalyzer
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import org.json.JSONObject

class RequestMessageHistory(
        val dialogId: String,
        val isChat: Boolean,
        val count: Int,
        val olderThanId: String = ""
) : Request("messages.getHistory") {
    init {
        val firstMessageIsUseless = olderThanId != ""
        val correctedCount = count + (if (firstMessageIsUseless) 1 else 0)

        params["count"] = correctedCount + 1
        params[if (isChat) "chat_id" else "user_id"] = dialogId
        if (olderThanId != "")
            params["start_message_id"] = olderThanId
    }

    override fun handleResponse(json: JSONObject) {
        val firstMessageIsUseless = olderThanId != ""

        val jsonMessageList = json.getJSONObject("response").getJSONArray("items")
        val messages = JSONParser parseMessages jsonMessageList
        if (firstMessageIsUseless && messages.isNotEmpty())
            messages.removeAt(0)

        loadMissingUsers(messages)
        loadMissingVideos(messages)

        MessageCaches.getCache(dialogId, isChat).putMessages(
                messages,
                allHistoryLoaded = messages.count() < count
        )
    }

    private fun loadMissingUsers(messages: Collection<Message>) {
        val missingIds = MissingDataAnalyzer.missingUsersIds(messages)
        if (missingIds.isNotEmpty())
            RequestControl addBackground RequestUsers(missingIds)
    }

    private fun loadMissingVideos(messages: Collection<Message>) {
        val missingIds = MissingDataAnalyzer.missingVideoIds(messages)
        if (missingIds.isNotEmpty())
            RequestControl addBackground RequestVideoUrls(dialogId, isChat, missingIds)
    }
}