package me.alexeyterekhov.vkfilter.DataCache.AttachedCache

import java.util.*

object AttachedCache {
    private val attachedToDialogs = HashMap<String, Attached>()
    private val attachedToChats = HashMap<String, Attached>()

    fun get(id: String, chat: Boolean): Attached {
        when {
            chat && !attachedToChats.contains(id) -> attachedToChats[id] = Attached(id, true)
            !chat && !attachedToDialogs.contains(id) -> attachedToDialogs[id] = Attached(id, false)
        }
        return if (chat) attachedToChats[id]!! else attachedToDialogs[id]!!
    }
}