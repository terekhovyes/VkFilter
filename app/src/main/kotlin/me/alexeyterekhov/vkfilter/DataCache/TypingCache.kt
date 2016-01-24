package me.alexeyterekhov.vkfilter.DataCache

import me.alexeyterekhov.vkfilter.DataCache.Common.forEachSync
import me.alexeyterekhov.vkfilter.DataClasses.UserTyping
import me.alexeyterekhov.vkfilter.Internet.Events.EventUserTyping
import me.alexeyterekhov.vkfilter.Util.EventBuses
import me.alexeyterekhov.vkfilter.Util.RunnableDelayer
import java.util.*

object TypingCache {
    private val AUTOSTOP_MILLIS = 8000L

    private val eventHelper = EventHelper()
    private val delayer = RunnableDelayer()
    private val typingCache = HashMap<String, LinkedList<UserTyping>>()
    val listeners = Vector<TypingListener>()

    init {
        val bus = EventBuses.longPollBus()
        if (!bus.isRegistered(eventHelper))
            bus.register(eventHelper)
    }

    fun getTyping(id: String, isChat: Boolean) = typingCache[cacheId(id, isChat)] ?: LinkedList()

    fun onUserStartTyping(id: String, isChat: Boolean, userId: String) {
        createIfNecessary(cacheId(id, isChat))
        if (typingCache[cacheId(id, isChat)]!!.none { it.userId == userId }) {
            val newItem = UserTyping(System.currentTimeMillis(), userId)
            typingCache[cacheId(id, isChat)]!!.add(0, newItem)
            listeners.forEachSync {
                it.onStartTyping(id, isChat, userId)
            }
        }
        delayer.delay(runnableId(id, isChat, userId), stopTypingRunnable(id, isChat, userId), AUTOSTOP_MILLIS)
    }

    fun onUserStopTyping(id: String, isChat: Boolean, userId: String) {
        createIfNecessary(cacheId(id, isChat))
        if (typingCache[cacheId(id, isChat)]!!.any { it.userId == userId }) {
            typingCache[cacheId(id, isChat)]!!.removeAll { it.userId == userId }
            listeners.forEachSync {
                it.onStopTyping(id, isChat, userId)
            }
        }
        removeIfNecessary(cacheId(id, isChat))
        delayer.cancel(runnableId(id, isChat, userId))
    }

    private fun cacheId(id: String, isChat: Boolean) = if (isChat) "c$id" else id

    private fun createIfNecessary(cacheId: String) {
        if (!typingCache.containsKey(cacheId))
            typingCache[cacheId] = LinkedList()
    }

    private fun removeIfNecessary(cacheId: String) {
        if (typingCache.containsKey(cacheId) && typingCache[cacheId]!!.isEmpty())
            typingCache.remove(cacheId)
    }

    private fun stopTypingRunnable(id: String, isChat: Boolean, userId: String) = Runnable {
        val cacheId = cacheId(id, isChat)
        if (typingCache.containsKey(cacheId) && typingCache[cacheId]!!.any { it.userId == userId }) {
            typingCache[cacheId]!!.removeAll { it.userId == userId }
            removeIfNecessary(cacheId)
            listeners.forEachSync {
                it.onStopTyping(id, isChat, userId)
            }
        }
    }

    private fun runnableId(id: String, isChat: Boolean, userId: String) = (if (isChat) "c$id" else id) + "_$userId"

    public interface TypingListener {
        fun onStartTyping(dialogId: String, isChat: Boolean, userId: String)
        fun onStopTyping(dialogId: String, isChat: Boolean, userId: String)
    }

    private class EventHelper() {
        public fun onEvent(e: EventUserTyping) {
            onUserStartTyping(e.dialogId.toString(), e.isChat, e.userId)
        }
    }
}