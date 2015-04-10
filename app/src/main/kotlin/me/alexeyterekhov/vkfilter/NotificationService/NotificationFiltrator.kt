package me.alexeyterekhov.vkfilter.NotificationService

import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import java.util.LinkedList


public object NotificationFiltrator {
    private var filters: List<VkFilter>? = null
    val filterListeners = LinkedList<DataDepend>()

    init {
        DAOFilters.changeListeners add object : DataDepend {
            override fun onDataUpdate() {
                loadFilters()
            }
        }
    }

    fun allowNotification(n: NotificationInfo): Boolean {
        if (filters == null)
            loadFilters()
        return when {
            filters == null -> true
            filters!!.isEmpty() -> true
            else -> {
                val allowers = filters!! filter { it.state == VkFilter.STATE_ALLOWING }
                val blockers = filters!! filter { it.state == VkFilter.STATE_BLOCKING }
                if (blockers none { contains(it, n) }
                    && (allowers.isEmpty()
                        || allowers any { contains(it, n) }))
                    true
                else
                    false
            }
        }
    }

    private fun loadFilters() {
        filters = DAOFilters.loadVkFilters()
        for (l in filterListeners)
            l.onDataUpdate()
    }

    private fun contains(f: VkFilter, n: NotificationInfo): Boolean {
        return f.identifiers() any { same(it, n) }
    }
    private fun same(id: VkIdentifier, n: NotificationInfo): Boolean {
        return when {
            id.type == VkIdentifier.TYPE_USER
                    && n.chatId == ""
                    && id.id.toString() == n.senderId -> true
            id.type == VkIdentifier.TYPE_CHAT
                    && id.id.toString() == n.chatId -> true
            else -> false
        }
    }
}