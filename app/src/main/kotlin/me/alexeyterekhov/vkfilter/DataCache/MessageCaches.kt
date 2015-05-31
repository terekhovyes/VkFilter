package me.alexeyterekhov.vkfilter.DataCache

import java.util.HashMap

object MessageCaches {
    private val dialogs = HashMap<String, MessageCache>()
    private val chats = HashMap<String, MessageCache>()

    fun getCache(id: String, chat: Boolean): MessageCache {
        when {
            chat && !chats.contains(id) -> chats[id] = MessageCache()
            !chat && !dialogs.contains(id) -> dialogs[id] = MessageCache()
        }
        return if (chat) $chats[id] else $dialogs[id]
    }
    fun deleteCache(id: String, chat: Boolean) {
        when {
            chat && chats contains id -> chats remove id
            !chat && dialogs contains id -> dialogs remove id
        }
    }
}