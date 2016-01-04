package me.alexeyterekhov.vkfilter.GUI.DialogsActivity.DialogList

import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.GUI.Mock.Mocker
import java.util.*

class DialogFiltrator {
    fun filterSnapshot(snapshot: DialogListSnapshot, filters: Collection<VkFilter>): Vector<Dialog> {
        if (Mocker.MOCK_MODE)
            return snapshot.dialogs

        val allowing = filters.filter { it.state == VkFilter.STATE_ALLOWING }
        val blocking = filters.filter { it.state == VkFilter.STATE_BLOCKING }
        val out = Vector<Dialog>()

        if (allowing.isNotEmpty()) {
            val ids = Vector<VkIdentifier>()
            for (f in allowing)
                ids.addAll(f.identifiers())
            out.addAll(snapshot.dialogs.filter { dialog -> ids.any { same(it, dialog) } })
        } else {
            out.addAll(snapshot.dialogs)
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

    private fun same(id: VkIdentifier, d: Dialog): Boolean {
        return id.id == d.id && (
                (id.type == VkIdentifier.TYPE_USER && !d.isChat()) ||
                        (id.type == VkIdentifier.TYPE_CHAT && d.isChat())
                )
    }
}