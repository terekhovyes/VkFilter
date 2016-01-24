package me.alexeyterekhov.vkfilter.GUI.DialogsActivity

import android.os.Handler
import android.text.TextUtils
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.DataCache.TypingCache
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.Internet.Events.EventMessageRead
import me.alexeyterekhov.vkfilter.Internet.LongPoll.LongPollControl
import java.util.*

class EventsModule(val activity: DialogsActivity) {
    private val handler = Handler()
    private val readMessageRunnables = HashMap<String, Runnable>()
    private val dialogCacheListener = createDialogCacheListener()
    private val typingCacheListener = createTypingCacheListener()

    fun onCreate() {
        TypingCache.listeners.add(typingCacheListener)
    }

    fun onResume() {
        DialogListCache.listeners.add(dialogCacheListener)
        LongPollControl.start()
        LongPollControl.eventBus().register(this)
    }

    fun onPause() {
        LongPollControl.stop()
        LongPollControl.eventBus().unregister(this)
        DialogListCache.listeners.remove(dialogCacheListener)
    }

    fun onDestroy() {
        TypingCache.listeners.remove(typingCacheListener)
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
            } else {
                newSnapshot.dialogs.add(dialog)
            }
        }

        if (listChanged)
            DialogListCache.updateSnapshot(newSnapshot)
    }

    private fun createDialogCacheListener() = object : DataDepend {
        override fun onDataUpdate() {
            val newSnap = DialogListCache.getSnapshot()

            val keyList = LinkedList(readMessageRunnables.keys)

            keyList.forEach { key ->
                val isChat = key.startsWith("c")
                val dialogId = (if (isChat) key.substring(1) else key).toLong()

                if (newSnap.dialogs.any {
                    it.isChat() == isChat
                            && it.id == dialogId
                            && TextUtils.isEmpty(it.activityMessage)
                }) {
                    readMessageRunnables.remove(key)
                }
            }
        }
    }

    private fun createTypingCacheListener() = object : TypingCache.TypingListener {
        override fun onStartTyping(dialogId: String, isChat: Boolean, userId: String) {
            activity.dialogListModule.getAdapter()?.checkForTyping(dialogId.toLong(), isChat)
        }

        override fun onStopTyping(dialogId: String, isChat: Boolean, userId: String) {
            activity.dialogListModule.getAdapter()?.checkForTyping(dialogId.toLong(), isChat)
        }
    }
}