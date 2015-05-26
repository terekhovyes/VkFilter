package me.alexeyterekhov.vkfilter.DataCache

import me.alexeyterekhov.vkfilter.DataCache.Helpers.MessagePack
import me.alexeyterekhov.vkfilter.GUI.Mock.Mocker
import java.util.HashMap

object MessageCacheOld {
    private val dialogs = HashMap<String, MessagePack>()
    private val chats = HashMap<String, MessagePack>()

    fun getDialog(id: String, chat: Boolean): MessagePack {
        if (Mocker.MOCK_MODE)
            return Mocker.mockMessagePack()
        when {
            chat && !chats.contains(id) -> chats[id] = MessagePack()
            !chat && !dialogs.contains(id) -> dialogs[id] = MessagePack()
        }
        return if (chat) $chats[id] else $dialogs[id]
    }
}