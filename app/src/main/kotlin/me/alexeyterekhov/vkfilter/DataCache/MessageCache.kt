package me.alexeyterekhov.vkfilter.DataCache

import me.alexeyterekhov.vkfilter.DataCache.Helpers.MessagePack
import java.util.HashMap

object MessageCache {
    private val dialogs = HashMap<String, MessagePack>()
    private val chats = HashMap<String, MessagePack>()

    fun getDialog(id: String, chat: Boolean): MessagePack {
        when {
            chat && !chats.contains(id) -> chats[id] = MessagePack()
            !chat && !dialogs.contains(id) -> dialogs[id] = MessagePack()
        }
        return if (chat) $chats[id] else $dialogs[id]
    }
}