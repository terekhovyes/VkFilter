package me.alexeyterekhov.vkfilter.DataCache

import android.os.Handler
import android.util.Log
import me.alexeyterekhov.vkfilter.DataCache.Helpers.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataClasses.Message
import java.util.Collections
import java.util.HashMap
import java.util.LinkedList

class MessageCache {
    fun <T> LinkedList<T>.forEachSync(action: (T) -> Unit) {
        val copy = LinkedList(this)
        copy forEach { action(it) }
    }

    private val concurrentActions = ConcurrentActions(delayMillis = 250, waitMillis = 300)
    private val messagesWithoutState = HashMap<Long, Message>()

    private val sentMessages = LinkedList<Message>()
    private val processingMessages = LinkedList<Message>()
    private var editMessage: Message = createEditMessage()
    val listeners = LinkedList<MessageCacheListener>()
    var historyLoaded = false
        private set
    var lastMessageIdFromServer = 0L
        private set

    fun getMessages(): Collection<Message> {
        val out = LinkedList(sentMessages)
        out addAll processingMessages
        return out
    }
    fun getEditMessage() = editMessage
    fun putMessages(messages: Collection<Message>, allHistoryLoaded: Boolean = false) {
        historyLoaded = allHistoryLoaded || historyLoaded
        val orderedMessages = if (messages.isNotEmpty() && messages.first().sentId > messages.last().sentId)
            messages.reverse()
        else
            messages
        if (orderedMessages.isNotEmpty())
            lastMessageIdFromServer = Math.max(lastMessageIdFromServer, orderedMessages.last().sentId)

        if (orderedMessages.isEmpty())
            Log.d("debug", "CACHE PUT MESSAGES ${orderedMessages.count()}")
        if (orderedMessages.count() == 1)
            Log.d("debug", "CACHE PUT MESSAGES ${orderedMessages.count()}: ${orderedMessages.first().sentId}")
        if (orderedMessages.count() > 1)
            Log.d("debug", "CACHE PUT MESSAGES ${orderedMessages.count()}: ${orderedMessages.first().sentId}..${orderedMessages.last().sentId}")

        // Prevent situation, when sent message still wasn't shown, but DialogRefresher already load it from server
        if (messages.isNotEmpty()
                && orderedMessages all { it.isOut }
                && (processingMessages.isNotEmpty()
                    || !messagesWithoutState.isEmpty())
                && orderedMessages.size() <= (processingMessages.count() + messagesWithoutState.count())
        ) {
            return
        }

        when {
            orderedMessages.isEmpty() -> {}
            sentMessages.isEmpty() -> putNewerSentMessages(orderedMessages)
            else -> {
                val curL = sentMessages.first().sentId
                val curR = sentMessages.last().sentId

                val older = orderedMessages filter { it.sentId < curL }
                val newer = orderedMessages filter { it.sentId > curR }
                val inner = orderedMessages filter { it.sentId in curL..curR }

                if (inner.isNotEmpty()) {
                    // Split inner messages on new and messages for replacement
                    val innerNewMessages = inner takeWhile {
                        val id = it.sentId
                        sentMessages none { it.sentId == id }
                    }
                    val replacement = inner drop innerNewMessages.count()

                    if (innerNewMessages.isNotEmpty()) putNewerSentMessages(innerNewMessages)
                    if (replacement.isNotEmpty()) replaceSentMessages(replacement)
                }
                if (newer.isNotEmpty()) putNewerSentMessages(newer)
                if (older.isNotEmpty()) putOlderSentMessages(older)
            }
        }
    }

    fun onWillSendMessage(guid: Long) {
        val sentMessage = editMessage
        sentMessage.sentId = guid
        editMessage = createEditMessage()
        messagesWithoutState.put(guid, sentMessage)
        concurrentActions.firstAction(guid, {
            if (messagesWithoutState contains guid) {
                Log.d("debug", "YAY! ACTION 1")
                val message = messagesWithoutState remove guid
                message.sentState = Message.STATE_PROCESSING
                processingMessages add message
                listeners forEachSync { it.onAddNewMessages(Collections.singleton(message)) }
            } else {
                Log.d("debug", "FOOO, ACTION 1 should be removed")
            }
        })
    }
    fun onDidSendMessage(guid: Long, sentId: Long) {
        concurrentActions.secondAction(
                guid,
                doIfFirstActionWaiting = {
                    Log.d("debug", "CANCEL ACTION 1! ACTION 2!")
                    val message = messagesWithoutState remove guid
                    message.sentState = Message.STATE_SENT
                    message.sentId = sentId
                    message.sentTimeMillis = System.currentTimeMillis()
                    sentMessages add message
                    listeners forEachSync { it.onAddNewMessages(Collections.singleton(message)) }
                },
                doIfFirstActionCalled = {
                    Log.d("debug", "ACTION 2 AFTER ACTION 1!")
                    val index = processingMessages indexOfFirst { it.sentId == guid }
                    val message = processingMessages remove index
                    message.sentState = Message.STATE_SENT
                    message.sentId = sentId
                    message.sentTimeMillis = System.currentTimeMillis()
                    if (sentMessages none { it.sentId == sentId }) {
                        sentMessages add message
                        listeners forEachSync { it.onUpdateMessages(Collections.singleton(message)) }
                    }
                }
        )
    }
    fun onReadMessages(out: Boolean, lastId: Long) {
        Log.d("debug", "CACHE READ ${if (out) "OUT" else "IN"} MESSAGES TO ID ${lastId}")
        val readMessages = sentMessages
                .reverse()
                .filter { it.isOut == out }
                .takeWhile { !it.isRead }
                .filter { it.sentId <= lastId }
        if (readMessages.isNotEmpty()) {
            readMessages forEach { it.isRead = true }
            listeners forEachSync { it onReadMessages readMessages }
        }
    }
    fun onUpdateMessages(updatedMessages: Collection<Message>) {
        val filtered = updatedMessages filter { sentMessages contains it }
        listeners forEachSync { it.onUpdateMessages(filtered) }
    }
    fun clearData() {
        sentMessages.clear()
        processingMessages.clear()
    }

    private fun putNewerSentMessages(messages: Collection<Message>) {
        messages forEach {
            val messageId = it.sentId
            val index = 1 + (sentMessages indexOfLast { it.sentId < messageId })
            sentMessages.add(index, it)
        }
        listeners forEachSync { it.onAddNewMessages(messages) }
    }
    private fun putOlderSentMessages(messages: Collection<Message>) {
        sentMessages.addAll(0, messages)
        listeners forEachSync { it.onAddOldMessages(messages) }
    }
    private fun replaceSentMessages(messages: Collection<Message>) {
        for (m in messages) {
            val index = sentMessages indexOfFirst { it.sentId == m.sentId }
            if (index >= 0) {
                val old = sentMessages[index]
                sentMessages.set(index, m)
                listeners forEachSync { it.onReplaceMessage(old, m) }
            }
        }
    }

    private fun createEditMessage(): Message {
        val m = Message(UserCache.getMyId())
        m.sentState = Message.STATE_IN_EDIT
        m.isOut = true
        m.isRead = false
        return m
    }
}

class ConcurrentActions(
        val delayMillis: Long,
        val waitMillis: Long
) {
    /*
    Object wait some time before calling action 1 and:
        1)  if action 2 called while action 1 was waiting
            object cancel action 1
            object execute action 2 immediately
        2)  if action 1 was called
            object execute action 2 in >= (delayMillis) after action 1
     */
    private val handler = Handler()
    private val firstActions = HashMap<Long, Runnable>()
    private val executionTime = HashMap<Long, Long>()

    fun firstAction(concurrentId: Long, action: () -> Unit) {
        val runnable = Runnable({
            executionTime[concurrentId] = System.currentTimeMillis()
            firstActions remove concurrentId
            action()
        })
        firstActions.put(concurrentId, runnable)
        handler.postDelayed(runnable, waitMillis)
    }
    fun secondAction(
            concurrentId: Long,
            doIfFirstActionWaiting: () -> Unit,
            doIfFirstActionCalled: () -> Unit
    ) {
        if (firstActions contains concurrentId) {
            handler removeCallbacks firstActions[concurrentId]
            doIfFirstActionWaiting()
        } else {
            val time = System.currentTimeMillis()
            val exec = executionTime remove concurrentId
            if (time - exec > delayMillis)
                doIfFirstActionCalled()
            else
                handler.postDelayed(doIfFirstActionCalled, exec + delayMillis - time)
        }
    }
}