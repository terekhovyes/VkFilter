package me.alexeyterekhov.vkfilter.DataCache

import java.util.Vector
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.DialogListSnapshot

object DialogListCache {
    public val listeners: Vector<DataDepend> = Vector()

    private var snapshot = emptySnapshot()

    fun updateSnapshot(snap: DialogListSnapshot) {
        snapshot = snap
        for (l in listeners) l.onDataUpdate()
    }

    fun getSnapshot() = snapshot

    fun clear() {
        snapshot = emptySnapshot()
    }

    fun emptySnapshot() = DialogListSnapshot(
            0,
            Vector<Dialog>()
    )
}