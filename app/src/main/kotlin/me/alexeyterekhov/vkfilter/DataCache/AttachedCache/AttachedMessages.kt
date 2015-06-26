package me.alexeyterekhov.vkfilter.DataCache.AttachedCache

import me.alexeyterekhov.vkfilter.DataCache.Common.forEachSync
import java.util.LinkedList

class AttachedMessages(val attached: Attached) {
    private val messagePacks = LinkedList<AttachedMessagePack>()

    fun add(pack: AttachedMessagePack) {
        messagePacks add pack
        attached.listeners forEachSync { it.onDataUpdate() }
    }

    fun remove(pack: AttachedMessagePack) {
        messagePacks remove pack
        attached.listeners forEachSync { it.onDataUpdate() }
    }

    fun clear() {
        messagePacks.clear()
        attached.listeners forEachSync { it.onDataUpdate() }
    }

    fun get() = messagePacks
}