package me.alexeyterekhov.vkfilter.Data.Cache

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.Dialog
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import java.util.*

object DialogCache {
    private val map = HashMap<DialogId, Dialog>()

    fun putDialog(dialog: Dialog) {
        map[dialog.id] = dialog
    }
    fun contains(id: DialogId): Boolean = map.containsKey(id)
    fun getDialog(id: DialogId): Dialog? = map[id]
    fun getDialogOrCreate(id: DialogId): Dialog {
        if (!map.containsKey(id))
            map.put(id, Dialog())
        return map[id]!!
    }
}