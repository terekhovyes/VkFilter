package me.alexeyterekhov.vkfilter.DataClasses

import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments


class Message(val senderId: String) {
    companion object {
        val STATE_SENT = 0
        val STATE_SENDING = 1
        val STATE_IN_EDIT = 2
        val STATE_TYPING = 3
    }

    // State
    var sentState = STATE_SENT
    var sentId = 0L
    var sentTimeMillis = 0L
    var isRead = false
    var isOut = false
    // Additional
    var isIn: Boolean
        get() = !isOut
        set(income) { isOut = !income }
    var isNotRead: Boolean
        get() = !isRead
        set(notRead) { isRead = !notRead }
    // Data
    var text = ""
    val attachments = Attachments()

    fun senderOrEmpty(): User = UserCache.getUser(senderId) ?: User()

    fun copy(): Message {
        val copy = Message(senderId)
        copy.sentState = sentState
        copy.sentId = sentId
        copy.sentTimeMillis = sentTimeMillis
        copy.isRead = isRead
        copy.isOut = isOut
        copy.text = text
        copy.attachments.copyFrom(attachments)
        return copy
    }
}