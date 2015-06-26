package me.alexeyterekhov.vkfilter.DataCache.AttachedCache

class AttachedMessagePack(
        val title: String,
        val messageIds: List<Long>,
        val dialogId: String,
        val isChat: Boolean
)