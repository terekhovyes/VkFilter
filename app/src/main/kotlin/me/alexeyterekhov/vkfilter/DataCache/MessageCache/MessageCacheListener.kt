package me.alexeyterekhov.vkfilter.DataCache.MessageCache

import me.alexeyterekhov.vkfilter.DataClasses.Message


public interface MessageCacheListener {
    fun onAddNewMessages(messages: Collection<Message>)
    fun onAddOldMessages(messages: Collection<Message>)
    fun onReplaceMessage(old: Message, new: Message)
    fun onUpdateMessages(messages: Collection<Message>)
    fun onReadMessages(messages: Collection<Message>)
}