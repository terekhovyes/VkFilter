package me.alexeyterekhov.vkfilter.Data.UpdateHandlers

import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Cache.UserCache
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.CurrentData
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.Dialog
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message
import me.alexeyterekhov.vkfilter.Data.Entities.Message.SentState
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.*
import me.alexeyterekhov.vkfilter.Data.Utils.ConcurrentActions
import me.alexeyterekhov.vkfilter.Data.Utils.MessageHolder
import me.alexeyterekhov.vkfilter.Data.Utils.MessageUtil
import me.alexeyterekhov.vkfilter.Util.EventBuses
import java.util.*

class MessageHandler {
    private val concurrentActions = ConcurrentActions(delayMillis = 250, waitMillis = 300)
    private val waitingMessages = MessageHolder()

    fun saveMessageHistory(dialogId: DialogId, messages: List<Message>, historyLoaded: Boolean) {
        val dialogMessages = DialogCache.getDialog(dialogId)!!.messages
        dialogMessages.historyCompletelyLoaded = dialogMessages.historyCompletelyLoaded || historyLoaded

        val sortedMessages = if (messages.isNotEmpty() && messages.first().sent.id > messages.last().sent.id)
            messages.reversed()
        else
            messages

        if (sortedMessages.isNotEmpty())
            dialogMessages.lastMessageIdFromServer = Math.max(dialogMessages.lastMessageIdFromServer, sortedMessages.last().sent.id)

        val dialogWaitingMessages = waitingMessages.getMessages(dialogId)
        if (sortedMessages.isNotEmpty()
                && sortedMessages.all { it.isOut }
                && (dialogMessages.sending.isNotEmpty() || dialogWaitingMessages.isNotEmpty())
                && sortedMessages.size <= (dialogMessages.sending.count() + dialogWaitingMessages.count())) {
            // Sent message wasn't shown in message list, but DialogRefresher has already loaded it from server
        } else {
            saveMessages(sortedMessages, DialogCache.getDialogOrCreate(dialogId))
        }
    }

    fun readMessages(dialog: Dialog, out: Boolean, lastId: Long) {
        val readMessages = dialog.messages.history
                .reversed()
                .filter { it.isOut == out }
                .takeWhile { !it.isRead }
                .filter { it.sent.id <= lastId }
        if (readMessages.isNotEmpty()) {
            readMessages.forEach { it.isRead = true }
            EventBuses.dataBus().post(EventMessagesRead(dialog.id, readMessages))
        }
    }

    fun saveLastMessageForEveryDialog(dialogList: List<Dialog>) {
        dialogList.forEach { updatedDialog ->
            val cachedDialog = DialogCache.getDialogOrCreate(updatedDialog.id)
            val indexOfOld = cachedDialog.messages.history.indexOfLast { it.sent.id == updatedDialog.messages.last!!.sent.id }

            if (indexOfOld >= 0) {
                if (!MessageUtil.messagesEquals(updatedDialog.messages.last!!, cachedDialog.messages.history[indexOfOld])) {
                    val newHistory = Vector(cachedDialog.messages.history)
                    newHistory[indexOfOld] = updatedDialog.messages.last!!
                    cachedDialog.messages.history = newHistory
                    EventBuses.dataBus().post(EventMessagesUpdated(cachedDialog.id, Collections.singletonList(newHistory[indexOfOld])))
                }
            } else {
                val newHistory = Vector(cachedDialog.messages.history)
                newHistory.add(updatedDialog.messages.last!!)
                cachedDialog.messages.history = newHistory
                EventBuses.dataBus().post(EventMessagesAddNew(cachedDialog.id, Collections.singletonList(updatedDialog.messages.last!!)))
            }
        }
    }

    fun sendMessage(dialogId: DialogId, guid: Long, sendingData: CurrentData) {
        val dialog = DialogCache.getDialogOrCreate(dialogId)
        val sentMessage = Message(UserCache.getMyId())

        sentMessage.sent.id = guid
        sentMessage.sent.state = SentState.State.STATE_SENDING
        sentMessage.isOut = true
        sentMessage.isNotRead = true
        sentMessage.data = MessageUtil.currentDataToMessageData(sendingData)

        onWillSendMessage(dialog, guid, sentMessage)

        dialog.current = CurrentData()
        EventBuses.dataBus().post(EventAttachmentsCleared(dialogId))
    }

    fun messageSent(dialogId: DialogId, guid: Long, sentId: Long) {
        val dialog = DialogCache.getDialogOrCreate(dialogId)
        concurrentActions.secondAction(
                guid,
                doIfFirstActionWaiting = {
                    val waitings = waitingMessages.getMessages(dialog.id)
                    val message = waitings.remove(guid)!!
                    waitingMessages.removeIfEmpty(dialog.id)

                    message.sent.state = SentState.State.STATE_SENT
                    message.sent.id = sentId
                    message.sent.timeMillis = System.currentTimeMillis()
                    val newHistory = Vector(dialog.messages.history)
                    newHistory.add(message)
                    dialog.messages.history = newHistory
                    EventBuses.dataBus().post(EventMessagesAddNew(dialog.id, Collections.singletonList(message)))
                },
                doIfFirstActionCalled = {
                    val newSending = Vector(dialog.messages.sending)
                    val newHistory = Vector(dialog.messages.history)

                    val index = newSending.indexOfFirst { it.sent.id == guid }
                    val message = newSending.removeAt(index)
                    message.sent.state = SentState.State.STATE_SENT
                    message.sent.id = sentId
                    message.sent.timeMillis = System.currentTimeMillis()

                    if (newHistory.none { it.sent.id == sentId }) {
                        newHistory.add(message)
                        dialog.messages.history = newHistory
                        dialog.messages.sending = newSending
                        EventBuses.dataBus().post(EventMessagesUpdated(dialog.id, Collections.singletonList(message)))
                    }
                }
        )
    }

    fun onWillSendMessage(dialog: Dialog, guid: Long, sentMessage: Message) {
        val waitings = waitingMessages.getMessages(dialog.id)
        sentMessage.sent.id = guid
        waitings.put(guid, sentMessage)
        concurrentActions.firstAction(guid, {
            val innerWaitings = waitingMessages.getMessages(dialog.id)
            if (innerWaitings.contains(guid)) {
                val message = innerWaitings.remove(guid)!!
                message.sent.state = SentState.State.STATE_SENDING
                val newSending = Vector(dialog.messages.sending)
                newSending.add(message)
                dialog.messages.sending = newSending
                EventBuses.dataBus().post(EventMessagesAddNew(dialog.id, Collections.singletonList(message)))
            }
            waitingMessages.removeIfEmpty(dialog.id)
        })
    }

    fun postMessagesUpdated(dialogId: DialogId, messages: List<Message>) = EventBuses.dataBus().post(EventMessagesUpdated(dialogId, messages))

    private fun saveMessages(messageList: List<Message>, dialog: Dialog) {
        when {
            messageList.isEmpty() -> {}
            dialog.messages.history.isEmpty() -> putNewerSentMessages(messageList, dialog)
            else -> {
                val curL = dialog.messages.history.first().sent.id
                val curR = dialog.messages.history.last().sent.id

                val older = messageList.filter { it.sent.id < curL }
                val newer = messageList.filter { it.sent.id > curR }
                val inner = messageList.filter { it.sent.id in curL..curR }

                if (inner.isNotEmpty()) {
                    // Split inner messages on new and messages for replacement
                    val innerNewMessages = inner.takeWhile {
                        val id = it.sent.id
                        dialog.messages.history.none { it.sent.id == id }
                    }
                    val replacement = inner.drop(innerNewMessages.count())

                    if (innerNewMessages.isNotEmpty()) putNewerSentMessages(innerNewMessages, dialog)
                    if (replacement.isNotEmpty()) replaceSentMessages(replacement, dialog)
                }
                if (newer.isNotEmpty()) putNewerSentMessages(newer, dialog)
                if (older.isNotEmpty()) putOlderSentMessages(older, dialog)
            }
        }
    }

    private fun putNewerSentMessages(messages: List<Message>, dialog: Dialog) {
        val newHistory = Vector(dialog.messages.history)

        messages.forEach {
            val messageId = it.sent.id
            val index = 1 + (newHistory.indexOfLast { it.sent.id < messageId })
            newHistory.add(index, it)
        }

        dialog.messages.history = newHistory
        EventBuses.dataBus().post(EventMessagesAddNew(dialog.id, messages))
    }

    private fun putOlderSentMessages(messages: List<Message>, dialog: Dialog) {
        val newHistory = Vector(dialog.messages.history)
        newHistory.addAll(0, messages)
        dialog.messages.history = newHistory
        EventBuses.dataBus().post(EventMessagesAddOld(dialog.id, messages))
    }

    private fun replaceSentMessages(messages: List<Message>, dialog: Dialog) {
        val newHistory = Vector(dialog.messages.history)
        val replacements = LinkedList<Pair<Message, Message>>()

        for (m in messages) {
            val index = newHistory.indexOfFirst { it.sent.id == m.sent.id }
            if (index >= 0) {
                val old = newHistory[index]
                newHistory[index] = m
                replacements.add(Pair(old, m))
            }
        }

        dialog.messages.history = newHistory
        replacements.forEach {
            EventBuses.dataBus().post(EventMessageReplaced(dialog.id, it.first, it.second))
        }
    }
}