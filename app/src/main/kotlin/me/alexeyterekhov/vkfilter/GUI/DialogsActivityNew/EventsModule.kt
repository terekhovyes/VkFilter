//package me.alexeyterekhov.vkfilter.GUI.DialogsActivity
//
//import android.os.Handler
//import android.text.TextUtils
//import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
//import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
//import me.alexeyterekhov.vkfilter.DataCache.UserCache
//import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.Dialog
//import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.DialogListSnapshot
//import me.alexeyterekhov.vkfilter.Internet.Events.EventMessageRead
//import me.alexeyterekhov.vkfilter.Internet.Events.EventUserTyping
//import me.alexeyterekhov.vkfilter.Internet.LongPoll.LongPollControl
//import me.alexeyterekhov.vkfilter.R
//import java.util.*
//
//class EventsModule(val activity: DialogsActivity) {
//    private val TYPING_PERIOD_MILLIS = 8000L
//    private val handler = Handler()
//    private val readMessageRunnables = HashMap<String, Runnable>()
//    private val dialogCacheListener = createDialogCacheListener()
//
//    fun onResume() {
//        DialogListCache.listeners.add(dialogCacheListener)
//        LongPollControl.start()
//        LongPollControl.eventBus().register(this)
//    }
//
//    fun onPause() {
//        LongPollControl.stop()
//        LongPollControl.eventBus().unregister(this)
//        DialogListCache.listeners.remove(dialogCacheListener)
//    }
//
//    public fun onEvent(readEvent: EventMessageRead) {
//        val listSnapshot = DialogListCache.getSnapshot()
//        val newSnapshot = DialogListSnapshot(System.currentTimeMillis(), Vector<Dialog>())
//        var listChanged = false
//
//        listSnapshot.dialogs.forEach { dialog ->
//            if (dialog.isChat() == readEvent.isChat
//                    && dialog.id == readEvent.dialogId
//                    && dialog.lastMessage != null
//                    && dialog.lastMessage!!.isIn == readEvent.incomes
//                    && dialog.lastMessage!!.sentId <= readEvent.lastMessageId) {
//                val changedDialog = dialog.copy()
//                changedDialog.lastMessage!!.isRead = true
//                newSnapshot.dialogs.add(changedDialog)
//                listChanged = true
//            } else {
//                newSnapshot.dialogs.add(dialog)
//            }
//        }
//
//        if (listChanged)
//            DialogListCache.updateSnapshot(newSnapshot)
//    }
//
//    public fun onEvent(readEvent: EventUserTyping) {
//        val runnableId = if (readEvent.isChat)
//            "c${readEvent.dialogId}"
//        else
//            readEvent.dialogId.toString()
//
//        if (readMessageRunnables.containsKey(runnableId)) {
//            val runnable = readMessageRunnables[runnableId]
//            handler.removeCallbacks(runnable)
//            handler.postDelayed(runnable, TYPING_PERIOD_MILLIS)
//        } else {
//            val runnable = Runnable {
//                updateActivityMessage(readEvent.dialogId, readEvent.isChat, "")
//                readMessageRunnables.remove(runnableId)
//            }
//
//            setDialogTyping(readEvent.dialogId, readEvent.isChat, readEvent.userId)
//            readMessageRunnables.put(runnableId, runnable)
//            handler.postDelayed(runnable, TYPING_PERIOD_MILLIS)
//        }
//    }
//
//    private fun setDialogTyping(dialogId: Long, isChat: Boolean, userId: String) {
//        val message = if (UserCache.contains(userId))
//            "${UserCache.getUser(userId)!!.firstName} ${activity.getString(R.string.chat_label_typing)}"
//        else
//            activity.getString(R.string.chat_label_typing)
//        updateActivityMessage(dialogId, isChat, message)
//    }
//
//    private fun updateActivityMessage(dialogId: Long, isChat: Boolean, message: String) {
//        val listSnapshot = DialogListCache.getSnapshot()
//        val newSnapshot = DialogListSnapshot(System.currentTimeMillis(), Vector<Dialog>())
//        var listChanged = false
//
//        listSnapshot.dialogs.forEach { dialog ->
//            if (dialog.id == dialogId
//                && dialog.isChat() == isChat
//                && !TextUtils.equals(message, dialog.activityMessage)) {
//                val changedDialog = dialog.copy()
//                changedDialog.activityMessage = message
//                newSnapshot.dialogs.add(changedDialog)
//                listChanged = true
//            } else {
//                newSnapshot.dialogs.add(dialog)
//            }
//        }
//
//        if (listChanged)
//            DialogListCache.updateSnapshot(newSnapshot)
//    }
//
//    private fun createDialogCacheListener() = object : DataDepend {
//        override fun onDataUpdate() {
//            val newSnap = DialogListCache.getSnapshot()
//
//            val keyList = LinkedList(readMessageRunnables.keys)
//
//            keyList.forEach { key ->
//                val isChat = key.startsWith("c")
//                val dialogId = (if (isChat) key.substring(1) else key).toLong()
//
//                if (newSnap.dialogs.any {
//                    it.isChat() == isChat
//                            && it.id == dialogId
//                            && TextUtils.isEmpty(it.activityMessage)
//                }) {
//                    readMessageRunnables.remove(key)
//                }
//            }
//        }
//    }
//}