package me.alexeyterekhov.vkfilter.DataCache.Helpers

import me.alexeyterekhov.vkfilter.DataClasses.Message


public interface MessageCacheListener {
    fun onAddNewMessages(count: Int)
    fun onAddOldMessages(count: Int)
    fun onReplaceMessage(old: Message, new: Message)
    fun onUpdateMessages(messages: Collection<Message>)
    fun onReadMessages(messages: Collection<Message>)
}