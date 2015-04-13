package me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogList

import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.GUI.Mock.Mocker
import java.util.Vector


object Filtrator {
    fun filter(snap: DialogListSnapshot, filters: List<VkFilter>): Vector<Dialog> {
        if (Mocker.MOCK_MODE)
            return snap.dialogs

        val allowing = filters filter { it.state == VkFilter.STATE_ALLOWING }
        val blocking = filters filter { it.state == VkFilter.STATE_BLOCKING }
        val out = Vector<Dialog>()

        if (allowing.isNotEmpty()) {
            val ids = Vector<VkIdentifier>()
            for (f in allowing)
                ids addAll f.identifiers()
            out addAll (snap.dialogs filter { dialog -> ids any { same(it, dialog) } })
        } else {
            out addAll snap.dialogs
        }

        if (blocking.isNotEmpty()) {
            val ids = Vector<VkIdentifier>()
            for (f in blocking)
                ids addAll f.identifiers()
            val blocked = out filter { dialog -> ids none { same(it, dialog) } }
            out.clear()
            out addAll blocked
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