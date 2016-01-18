package me.alexeyterekhov.vkfilter.Data.UpdateHandlers

import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Cache.DialogListCache
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.Dialog
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventDialogListIncreased
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventDialogListReloaded
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventDialogsUpdated
import me.alexeyterekhov.vkfilter.Data.Utils.DialogUtil
import me.alexeyterekhov.vkfilter.Util.EventBuses
import java.util.*

class DialogHandler {
    fun updateChats(chats: Collection<Dialog>) = updateDialogs(chats)

    fun updateDialogs(dialogs: Collection<Dialog>) {
        dialogs.forEach {
            if (DialogCache.contains(it.id)) {
                val mergedDialog = DialogUtil.mergeDialogInformation(DialogCache.getDialog(it.id)!!, it)
                DialogCache.putDialog(mergedDialog)
            } else
                DialogCache.putDialog(it)
        }
        val updatedDialogs = dialogs.map { DialogCache.getDialog(it.id)!! }
        EventBuses.dataBus().post(EventDialogsUpdated(updatedDialogs))
    }

    fun updateDialogList(dialogs: List<Dialog>, offset: Int) {
        updateDialogs(dialogs)
        UpdateHandler.messages.saveLastMessageForEveryDialog(dialogs)

        val idList = dialogs.map { it.id }
        if (offset == 0) {
            DialogListCache.list = idList
            DialogListCache.updateTimeMillis = System.currentTimeMillis()
            EventBuses.dataBus().post(EventDialogListReloaded(idList))
        } else {
            val updatedList = Vector(DialogListCache.list.subList(0, offset))
            updatedList.addAll(idList)
            DialogListCache.list = updatedList
            DialogListCache.updateTimeMillis = System.currentTimeMillis()
            EventBuses.dataBus().post(EventDialogListIncreased(idList, offset))
        }
    }
}