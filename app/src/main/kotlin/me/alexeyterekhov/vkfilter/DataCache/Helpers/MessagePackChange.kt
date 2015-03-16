package me.alexeyterekhov.vkfilter.DataCache.Helpers

class MessagePackChange (
        val addedMessagesCount: Int,
        val addedMessagesAreNew: Boolean,
        val markedMessagesAreIncomes: Boolean,
        val markedFrom: Long,
        val markedTo: Long
)