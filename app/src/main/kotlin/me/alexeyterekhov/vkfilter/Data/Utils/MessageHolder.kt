package me.alexeyterekhov.vkfilter.Data.Utils

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message
import java.util.*

class MessageHolder {
    private val holder = HashMap<DialogId, MutableMap<Long, Message>>()

    fun removeIfEmpty(dialogId: DialogId) {
        if (holder[dialogId]?.isEmpty() == true)
            holder.remove(dialogId)
    }

    fun getMessages(dialogId: DialogId): MutableMap<Long, Message> {
        if (!holder.containsKey(dialogId))
            holder[dialogId] = HashMap()
        return holder[dialogId]!!
    }
}