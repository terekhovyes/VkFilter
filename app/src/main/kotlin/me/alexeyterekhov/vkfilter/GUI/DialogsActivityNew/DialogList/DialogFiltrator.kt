package me.alexeyterekhov.vkfilter.GUI.DialogsActivityNew.DialogList

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import java.util.*

class DialogFiltrator {
    fun filterSnapshot(dialogIds: List<DialogId>, filters: Collection<VkFilter>): List<DialogId> {
        val allowing = filters.filter { it.state == VkFilter.STATE_ALLOWING }
        val blocking = filters.filter { it.state == VkFilter.STATE_BLOCKING }
        val out = Vector<DialogId>()

        if (allowing.isNotEmpty()) {
            val ids = Vector<VkIdentifier>()
            for (f in allowing)
                ids.addAll(f.identifiers())
            out.addAll(dialogIds.filter { dialog -> ids.any { same(it, dialog) } })
        } else {
            out.addAll(dialogIds)
        }

        if (blocking.isNotEmpty()) {
            val ids = Vector<VkIdentifier>()
            for (f in blocking)
                ids.addAll(f.identifiers())
            val blocked = out.filter { dialog -> ids.none { same(it, dialog) } }
            out.clear()
            out.addAll(blocked)
        }

        return out
    }

    private fun same(id: VkIdentifier, d: DialogId): Boolean {
        return id.id == d.id && (
                (id.type == VkIdentifier.TYPE_USER && !d.isChat) ||
                        (id.type == VkIdentifier.TYPE_CHAT && d.isChat)
                )
    }
}