package me.alexeyterekhov.vkfilter.InternetNew.Requests

import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.InternetNew.JSONParser
import me.alexeyterekhov.vkfilter.InternetNew.RequestControl
import org.json.JSONObject
import java.util.LinkedList
import java.util.Vector

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

        val jsonMessageList = json getJSONObject "response" getJSONArray "items"
        val messages = JSONParser parseMessages jsonMessageList
        if (firstMessageIsUseless && messages.isNotEmpty())
            messages remove 0

        loadNotLoadedUsers(messages)
        loadNotLoadedVideos(messages)

        MessageCaches.getCache(dialogId, isChat).putMessages(
                messages,
                allHistoryLoaded = messages.count() < count
        )
    }

    private fun loadNotLoadedUsers(messages: Vector<Message>) {
        val notLoadedIds = messages
                .map { notLoadedUserIds(it) }
                .foldRight(LinkedList<String>(), {
                    list, el ->
                    list addAll el
                    list
                })

        if (notLoadedIds.isNotEmpty())
            RequestControl addBackground RequestUsers(notLoadedIds)
    }

    private fun notLoadedUserIds(m: Message): LinkedList<String> {
        val list = if (m.attachments.messages.isNotEmpty())
            m.attachments.messages
                    .map { notLoadedUserIds(it) }
                    .foldRight(LinkedList<String>(), {
                        list, el ->
                        list addAll el
                        list
                    })
        else
            LinkedList<String>()

        if (!UserCache.contains(m.senderId))
            list add m.senderId
        return list
    }

    private fun loadNotLoadedVideos(messages: Vector<Message>) {
        val notLoadedIds = messages
                .map { notLoadedVideoIds(it) }
                .foldRight(LinkedList<String>(), {
                    list, el ->
                    list addAll el
                    list
                })
        if (notLoadedIds.isNotEmpty())
            RequestControl addBackground RequestVideoUrls(dialogId, isChat, notLoadedIds)
    }

    private fun notLoadedVideoIds(m: Message): LinkedList<String> {
        val list = if (m.attachments.messages.isNotEmpty())
            m.attachments.messages
                    .map { notLoadedVideoIds(it) }
                    .foldRight(LinkedList<String>(), {
                        list, el ->
                        list addAll el
                        list
                    })
        else
            LinkedList<String>()
        m.attachments.videos filter { it.playerUrl == "" } forEach {
            list add it.requestKey
        }
        return list
    }
}