package me.alexeyterekhov.vkfilter.GUI.DialogsActivity

import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.Internet.Events.EventMessageRead
import me.alexeyterekhov.vkfilter.Internet.LongPoll.LongPollControl
import java.util.*

class EventsModule(val activity: DialogsActivity) {
    fun onResume() {
        LongPollControl.start()
        LongPollControl.eventBus().register(this)
    }

    fun onPause() {
        LongPollControl.stop()
        LongPollControl.eventBus().unregister(this)
    }

    public fun onEvent(readEvent: EventMessageRead) {
        val listSnapshot = DialogListCache.getSnapshot()
        val newSnapshot = DialogListSnapshot(System.currentTimeMillis(), Vector<Dialog>())
        var listChanged = false

        listSnapshot.dialogs.forEach { dialog ->
            if (dialog.isChat() == readEvent.isChat
                    && dialog.id == readEvent.dialogId
                    && dialog.lastMessage != null
                    && dialog.lastMessage!!.isIn == readEvent.incomes
                    && dialog.lastMessage!!.sentId <= readEvent.lastMessageId) {
                val changedDialog = dialog.copy()
                changedDialog.lastMessage!!.isRead = true
                newSnapshot.dialogs.add(changedDialog)
                listChanged = true
            } else
                newSnapshot.dialogs.add(dialog)
        }

        if (listChanged)
            DialogListCache.updateSnapshot(newSnapshot)
    }
}