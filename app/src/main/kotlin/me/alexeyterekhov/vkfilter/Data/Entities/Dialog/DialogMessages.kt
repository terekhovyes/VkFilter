package me.alexeyterekhov.vkfilter.Data.Entities.Dialog

import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message
import java.util.*

class DialogMessages : Cloneable {
    var history: List<Message> = LinkedList()
    var sending: List<Message> = LinkedList()
    var historyCompletelyLoaded = false
    var lastMessageIdFromServer = 0L
    var last: Message?
        get() = history.lastOrNull()
        private set(value) {}

    override public fun clone(): Any {
        val copy = DialogMessages()

        copy.history = copyList(history)
        copy.sending = copyList(sending)
        copy.historyCompletelyLoaded = historyCompletelyLoaded
        copy.lastMessageIdFromServer = lastMessageIdFromServer

        return copy
    }

    private fun <Type>copyList(list: List<Cloneable>): List<Type> {
        val copy = Vector<Type>()

        list.forEach { copy.add(it.clone() as Type) }

        return copy
    }
}