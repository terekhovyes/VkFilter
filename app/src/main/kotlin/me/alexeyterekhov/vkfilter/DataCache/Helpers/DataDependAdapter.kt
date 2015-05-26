package me.alexeyterekhov.vkfilter.DataCache.Helpers

import me.alexeyterekhov.vkfilter.DataClasses.MessageNew

public abstract class DataDependAdapter():
        MessageCacheListener,
        DataDepend
{
    final override fun onAddNewMessages(count: Int) = onDataUpdate()
    final override fun onAddOldMessages(count: Int) = onDataUpdate()
    final override fun onReplaceMessage(old: MessageNew, new: MessageNew) = onDataUpdate()
    final override fun onUpdateMessages(messages: Collection<MessageNew>) = onDataUpdate()
    final override fun onReadMessages(messages: Collection<MessageNew>) = onDataUpdate()
}