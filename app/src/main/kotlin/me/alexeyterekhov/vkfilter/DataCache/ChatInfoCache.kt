package me.alexeyterekhov.vkfilter.DataCache

import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataClasses.ChatInfo
import java.util.HashMap
import java.util.Vector


object ChatInfoCache {
    private val map = HashMap<String, ChatInfo>()

    public val listeners: Vector<DataDepend> = Vector()

    public fun dataUpdated() {
        for (l in listeners)
            l.onDataUpdate()
    }
    public fun putChat(id: String, chat: ChatInfo) {
        map[id] = chat
    }
    public fun contains(id: String): Boolean = map.containsKey(id)
    public fun getChat(id: String): ChatInfo? = map[id]
}