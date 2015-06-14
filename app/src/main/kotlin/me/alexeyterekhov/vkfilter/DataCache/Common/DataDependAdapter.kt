package me.alexeyterekhov.vkfilter.DataCache.Common

import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataClasses.Message

public abstract class DataDependAdapter():
        MessageCacheListener,
        DataDepend
{
    final override fun onAddNewMessages(messages: Collection<Message>) = onDataUpdate()
    final override fun onAddOldMessages(messages: Collection<Message>) = onDataUpdate()
    final override fun onReplaceMessage(old: Message, new: Message) = onDataUpdate()
    final override fun onUpdateMessages(messages: Collection<Message>) = onDataUpdate()
    final override fun onReadMessages(messages: Collection<Message>) = onDataUpdate()
}