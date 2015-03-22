package me.alexeyterekhov.vkfilter.DataCache.Helpers

import android.util.Log
import me.alexeyterekhov.vkfilter.DataClasses.Message
import java.util.HashSet
import java.util.LinkedList

class MessagePack {
    val messages = LinkedList<Message>()
    val listeners = LinkedList<DataDepend>()

    var allHistoryLoaded = false
        private set
    var info = MessagePackChange(0, false, false, 0, 0)
        private set
    // > 0 when added history < 0 when added new messages

    fun addMessagesWithReplace(msgs: Collection<Message>, itsAll: Boolean) {
        allHistoryLoaded = itsAll || allHistoryLoaded

        val collection = if (!msgs.isEmpty() && msgs.first().id > msgs.last().id)
            msgs.reverse() else msgs
        when {
            collection.isEmpty() -> {
                info = MessagePackChange(
                        addedMessagesCount = 0,
                        addedMessagesAreNew = false,
                        markedMessagesAreIncomes = false,
                        markedFrom = 0,
                        markedTo = 0
                )
            }
            messages.isEmpty() -> {
                messages.addAll(collection)
                info = MessagePackChange(
                        addedMessagesCount = collection.size(),
                        addedMessagesAreNew = true,
                        markedMessagesAreIncomes = false,
                        markedFrom = 0,
                        markedTo = 0
                )
            }
            else -> {
                val left = messages.first().id
                val right = messages.last().id
                val l = collection.first().id
                val r = collection.last().id
                when {
                    r < left -> {
                        messages.addAll(0, collection)
                        info = MessagePackChange(
                                addedMessagesCount = collection.size(),
                                addedMessagesAreNew = false,
                                markedMessagesAreIncomes = false,
                                markedFrom = 0,
                                markedTo = 0
                        )
                    }
                    l < left && r in left..right -> {
                        val forAdd = collection filter { it.id < left }
                        val forReplace = collection filter { it.id >= left }
                        var deleted = 0
                        while (!messages.isEmpty() && messages.first().id <= r) {
                            messages.removeFirst()
                            ++deleted
                        }
                        messages.addAll(0, forReplace)
                        messages.addAll(0, forAdd)
                        val addedCount = forAdd.size() + forReplace.size() - deleted
                        info = MessagePackChange(
                                addedMessagesCount = Math.abs(addedCount),
                                addedMessagesAreNew = addedCount < 0,
                                markedMessagesAreIncomes = false,
                                markedFrom = 0,
                                markedTo = 0
                        )
                    }
                    left <= l && r <= right -> {
                        val from = messages.indexOf(messages first { it.id >= l })
                        var deleted = 0
                        val alreadyRead = HashSet<Long>()
                        while (messages.size() > from && messages.get(from).id <= r) {
                            val m = messages.remove(from)
                            if (m.isRead)
                                alreadyRead add m.id
                            ++deleted
                        }
                        for (m in collection)
                            if (alreadyRead contains m.id)
                                m.isRead = true
                        messages.addAll(from, collection)
                        val addedCount = collection.size() - deleted
                        info = MessagePackChange(
                                addedMessagesCount = Math.abs(addedCount),
                                addedMessagesAreNew = addedCount < 0,
                                markedMessagesAreIncomes = false,
                                markedFrom = 0,
                                markedTo = 0
                        )
                    }
                    l in left..right && r > right -> {
                        val forAdd = collection filter { it.id > right }
                        val forReplace = collection filter { it.id <= right }
                        var deleted = 0
                        val alreadyRead = HashSet<Long>()
                        while (!messages.isEmpty() && messages.last().id >= l) {
                            val m = messages.removeLast()
                            if (m.isRead)
                                alreadyRead add m.id
                            ++deleted
                        }
                        for (m in collection)
                            if (alreadyRead contains m.id)
                                m.isRead = true
                        messages.addAll(forReplace)
                        messages.addAll(forAdd)
                        val addedCount = -(forAdd.size() + forReplace.size() - deleted)
                        info = MessagePackChange(
                                addedMessagesCount = Math.abs(addedCount),
                                addedMessagesAreNew = addedCount < 0,
                                markedMessagesAreIncomes = false,
                                markedFrom = 0,
                                markedTo = 0
                        )
                    }
                    l > right -> {
                        messages.addAll(collection)
                        info = MessagePackChange(
                                addedMessagesCount = collection.size(),
                                addedMessagesAreNew = true,
                                markedMessagesAreIncomes = false,
                                markedFrom = 0,
                                markedTo = 0
                        )
                    }
                    l < left && right < r -> {
                        val leftPart = collection filter { it.id <= right }
                        val rightPart = collection filter { it.id > right }
                        val addedCount1 = leftPart.size() - messages.size()
                        messages.clear()
                        messages.addAll(leftPart)
                        info = MessagePackChange(
                                addedMessagesCount = Math.abs(addedCount1),
                                addedMessagesAreNew = addedCount1 < 0,
                                markedMessagesAreIncomes = false,
                                markedFrom = 0,
                                markedTo = 0
                        )
                        for (lis in listeners) lis.onDataUpdate()
                        messages.addAll(rightPart)
                        val addedCount2 = -rightPart.size()
                        info = MessagePackChange(
                                addedMessagesCount = Math.abs(addedCount2),
                                addedMessagesAreNew = addedCount2 < 0,
                                markedMessagesAreIncomes = false,
                                markedFrom = 0,
                                markedTo = 0
                        )
                    }
                }
            }
        }
        for (lis in listeners) lis.onDataUpdate()
    }

    fun markIncomesAsRead() {
        var fromId = 0L
        var toId = 0L
        val it = messages.descendingIterator()
        while (it.hasNext()) {
            val message = it.next()
            if (message.isOut)
                continue
            if (message.isRead)
                break
            if (toId == 0L)
                toId = message.id
            fromId = message.id
            message.isRead = true
        }
        if (fromId != 0L && toId != 0L) {
            info = MessagePackChange(
                    addedMessagesCount = 0,
                    addedMessagesAreNew = false,
                    markedMessagesAreIncomes = true,
                    markedFrom = fromId,
                    markedTo = toId
            )
            for (lis in listeners) lis.onDataUpdate()
        }
    }

    fun markOutcomesAsRead(lastReadId: Long) {
        var fromId = 0L
        for (m in messages.reverse()) {
            if (!m.isOut || m.id > lastReadId) continue
            if (m.isRead) break
            else {
                fromId = m.id
                m.isRead = true
            }
        }
        if (fromId != 0L) {
            info = MessagePackChange(
                    addedMessagesCount = 0,
                    addedMessagesAreNew = false,
                    markedMessagesAreIncomes = false,
                    markedFrom = fromId,
                    markedTo = lastReadId
            )
            for (lis in listeners) lis.onDataUpdate()
        }
    }

    private fun logLastMessages(count: Int) {
        Log.d("debug", "==== LAST $count MESSAGES ====")
        for (i in messages.size() - count - 1 .. messages.size() - 1) {
            if (i < 0)
                continue
            val m = messages.get(i)
            Log.d("debug", "${ if (m.isOut) "O" else "I"}${m.id}, state ${if (m.isRead) "READ" else "NOT"}")
        }
        Log.d("debug", "==== ==================== ====")
    }
}