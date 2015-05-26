package me.alexeyterekhov.vkfilter.DataCache

import android.os.Handler
import android.util.Log
import me.alexeyterekhov.vkfilter.DataCache.Helpers.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataClasses.MessageNew
import java.util.Collections
import java.util.HashMap
import java.util.LinkedList

fun <T> LinkedList<T>.forEachSync(action: (T) -> Unit) {
    val copy = LinkedList(this)
    copy forEach { action(it) }
}

class MessageCache {
    private val concurrentActions = ConcurrentActions(delayMillis = 250, waitMillis = 300)
    private val messagesWithoutState = HashMap<Long, MessageNew>()

    private val sentMessages = LinkedList<MessageNew>()
    private val processingMessages = LinkedList<MessageNew>()
    private var editMessage: MessageNew = createEditMessage()
    val listeners = LinkedList<MessageCacheListener>()
    var historyLoaded = false
        private set

    fun getMessages(): Collection<MessageNew> {
        val out = LinkedList(sentMessages)
        out addAll processingMessages
        return out
    }
    fun getEditMessage() = editMessage
    fun putMessages(messages: Collection<MessageNew>, allHistoryLoaded: Boolean = false) {
        Log.d("debug", "CACHE PUT MESSAGES ${messages.count()}")
        historyLoaded = allHistoryLoaded || historyLoaded
        val orderedMessages = if (messages.isNotEmpty() && messages.first().sentId > messages.last().sentId)
            messages.reverse()
        else
            messages

        if (orderedMessages.count() == 1
                && orderedMessages.first().isOut
                && (!messagesWithoutState.isEmpty() || processingMessages.isNotEmpty()))
            return

        when {
            orderedMessages.isEmpty() -> {}
            sentMessages.isEmpty() -> putNewerSentMessages(orderedMessages)
            else -> {
                val curL = sentMessages.first().sentId
                val curR = sentMessages.last().sentId

                val older = orderedMessages filter { it.sentId < curL }
                val newer = orderedMessages filter { it.sentId > curR }
                val replacement = orderedMessages filter { it.sentId in curL..curR }

                if (replacement.isNotEmpty()) replaceSentMessages(replacement)
                if (newer.isNotEmpty()) putNewerSentMessages(newer)
                if (older.isNotEmpty()) putOlderSentMessages(older)
            }
        }
    }

    fun onStartSending(guid: Long) {
        val sentMessage = editMessage
        editMessage = createEditMessage()
        messagesWithoutState.put(guid, sentMessage)
        concurrentActions.firstAction(guid, {
            if (messagesWithoutState contains guid) {
                Log.d("debug", "YAY! ACTION 1")
                val message = messagesWithoutState remove guid
                message.sentState = MessageNew.STATE_PROCESSING
                processingMessages add message
                listeners forEachSync { it.onAddNewMessages(1) }
            } else {
                Log.d("debug", "FOOO, ACTION 1 should be removed")
            }
        })
    }
    fun onFinishSending(guid: Long, sentId: Long) {
        concurrentActions.secondAction(
                guid,
                doIfFirstActionWaiting = {
                    Log.d("debug", "CANCEL ACTION 1! ACTION 2!")
                    val message = messagesWithoutState remove guid
                    message.sentState = MessageNew.STATE_SENT
                    message.sentId = sentId
                    message.sentTimeMillis = System.currentTimeMillis()
                    sentMessages add message
                    listeners forEachSync { it.onAddNewMessages(1) }
                },
                doIfFirstActionCalled = {
                    Log.d("debug", "ACTION 2 AFTER ACTION 1!")
                    val index = processingMessages indexOfFirst { it.sentId == guid }
                    val message = processingMessages remove index
                    message.sentState = MessageNew.STATE_SENT
                    message.sentId = sentId
                    message.sentTimeMillis = System.currentTimeMillis()
                    sentMessages add message
                    listeners forEachSync { it.onUpdateMessages(Collections.singleton(message)) }
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

    private fun putNewerSentMessages(messages: Collection<MessageNew>) {
        sentMessages addAll messages
        listeners forEachSync { it.onAddNewMessages(messages.count()) }
    }
    private fun putOlderSentMessages(messages: Collection<MessageNew>) {
        sentMessages.addAll(0, messages)
        listeners forEachSync { it.onAddOldMessages(messages.count()) }
    }
    private fun replaceSentMessages(messages: Collection<MessageNew>) {
        for (m in messages) {
            val index = sentMessages indexOfFirst { it.sentId == m.sentId }
            if (index >= 0) {
                val old = sentMessages[index]
                sentMessages.set(index, m)
                listeners forEachSync { it.onReplaceMessage(old, m) }
            }
        }
    }

    private fun createEditMessage(): MessageNew {
        val m = MessageNew(UserCache.getMyId())
        m.sentState = MessageNew.STATE_IN_EDIT
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