package me.alexeyterekhov.vkfilter.DataCache

import android.os.Handler
import java.util.*

object TypingCache {
    private val AUTOSTOP_MILLIS = 8000L

    private val handler = Handler()
    private val chatRunnables = HashMap<String, HashMap<String, Runnable>>()
    private val dialogRunnables = HashMap<String, HashMap<String, Runnable>>()
    private val chatTyping = HashMap<String, HashSet<String>>()
    private val dialogTyping = HashMap<String, HashSet<String>>()
    public val listeners = Vector<TypingListener>()

    fun onUserStartTyping(id: String, isChat: Boolean, userId: String) {
        val cache = getCache(isChat)
        createIfNecessary(id, cache)
        if (!cache[id]!!.contains(userId)) {
            cache[id]!!.add(userId)
            listeners.forEach {
                it.onStartTyping(id, isChat, userId)
            }
        }
    }

    fun onUserStopTyping(id: String, isChat: Boolean, userId: String) {
        val cache = getCache(isChat)
        createIfNecessary(id, cache)
        if (!cache[id]!!.contains(userId)) {
            cache[id]!!.remove(userId)
            listeners.forEach {
                it.onStopTyping(id, isChat, userId)
            }
        }
        removeIfNecessary(id, cache)
    }

    private fun createIfNecessary(id: String, cache: HashMap<String, HashSet<String>>) {
        if (!cache.containsKey(id))
            cache[id] = HashSet()
    }

    private fun removeIfNecessary(id: String, cache: HashMap<String, HashSet<String>>) {
        if (cache.contains(id) && cache[id]!!.isEmpty())
            chatTyping.remove(id)
    }

    private fun createRunnableIfNecessary(id: String, cache: HashMap<String, HashMap<String, Runnable>>) {
        if (!cache.containsKey(id))
            cache[id] = HashMap()
    }

    private fun removeRunnableIfNecessary(id: String, cache: HashMap<String, HashMap<String, Runnable>>) {
        if (cache.contains(id) && cache[id]!!.isEmpty())
            chatTyping.remove(id)
    }

    private fun scheduleAutostop(id: String, isChat: Boolean, userId: String) {
        val cache = getRunnableCache(isChat)
        createRunnableIfNecessary(id, cache)

    }

    private fun cancelIfExists(id: String, isChat: Boolean, userId: String) {
        val cache = getRunnableCache(isChat)
        if (cache.contains(id) && cache[id]!!.contains(userId)) {

        }
    }

    private fun getCache(isChat: Boolean) = if (isChat) chatTyping else dialogTyping
    private fun getRunnableCache(isChat: Boolean) = if (isChat) chatRunnables else dialogRunnables

    public interface TypingListener {
        fun onStartTyping(dialogId: String, isChat: Boolean, userId: String)
        fun onStopTyping(dialogId: String, isChat: Boolean, userId: String)
    }
}