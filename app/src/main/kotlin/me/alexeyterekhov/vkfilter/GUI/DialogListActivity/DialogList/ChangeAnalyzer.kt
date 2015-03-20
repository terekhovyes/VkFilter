package me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogList

import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.DialogListSnapshot

object ChangeAnalyzer {
    fun countUpdatedDialogs(old: DialogListSnapshot, new: DialogListSnapshot): Int {
        when {
            old.dialogs.isEmpty() -> return new.dialogs.size()
            else -> {
                for (d in old.dialogs) {
                    val d2 = new.dialogs.firstOrNull {
                        it.id == d.id && it.lastMessage!!.dateMSC == d.lastMessage!!.dateMSC
                    }
                    if (d2 != null)
                        return new.dialogs indexOf d2
                }
                return new.dialogs.size()
            }
        }
    }

    // Returns old position or -1 if dialog wasn't found
    fun findOldPositionOfDialog(old: DialogListSnapshot, new: DialogListSnapshot, newPos: Int): Int {
        val id = new.dialogs[newPos].id
        return if (old.dialogs.none { it.id == id })
            -1
        else
            old.dialogs indexOf old.dialogs.first { it.id == id }
    }

    fun findPositionOfDialog(snap: DialogListSnapshot, d: Dialog): Int {
        val id = d.id
        for (i in 0..snap.dialogs.size() - 1)
            if (snap.dialogs[i].id == id)
                return i
        return -1
    }
}