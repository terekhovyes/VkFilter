package me.alexeyterekhov.vkfilter.DataCache.Helpers

import me.alexeyterekhov.vkfilter.DataClasses.MessageNew


public trait MessageCacheListener {
    fun onAddNewMessages(count: Int)
    fun onAddOldMessages(count: Int)
    fun onReplaceMessage(old: MessageNew, new: MessageNew)
    fun onUpdateMessages(messages: Collection<MessageNew>)
    fun onReadMessages(messages: Collection<MessageNew>)
}