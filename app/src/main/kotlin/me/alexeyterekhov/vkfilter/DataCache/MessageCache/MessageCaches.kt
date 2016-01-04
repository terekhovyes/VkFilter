package me.alexeyterekhov.vkfilter.DataCache.MessageCache

import java.util.*

object MessageCaches {
    private val dialogs = HashMap<String, MessageCache>()
    private val chats = HashMap<String, MessageCache>()

    fun getCache(id: String, chat: Boolean): MessageCache {
        when {
            chat && !chats.contains(id) -> chats[id] = MessageCache()
            !chat && !dialogs.contains(id) -> dialogs[id] = MessageCache()
        }
        return if (chat) chats[id]!! else dialogs[id]!!
    }
}