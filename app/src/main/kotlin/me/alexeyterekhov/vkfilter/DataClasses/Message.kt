package me.alexeyterekhov.vkfilter.DataClasses

import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments

class Message(val senderId: String) {
    var id = 0L
    // State
    var isRead = false
    var isOut = false
    // Date
    var dateMSC = 0L
    // Data
    var text = ""
    val attachments = Attachments()

    fun senderOrEmpty(): User = UserCache.getUser(senderId) ?: User()

    fun toNewFormat(): MessageNew {
        val msg = MessageNew(senderId)
        msg.sentId = id
        msg.sentState = MessageNew.STATE_SENT
        msg.isRead = isRead
        msg.isOut = isOut
        msg.sentTimeMillis = dateMSC
        msg.text = text
        with (msg.attachments) {
            audios addAll attachments.audios
            images addAll attachments.images
            videos addAll attachments.videos
            documents addAll attachments.documents
            messages addAll attachments.messages
        }
        return msg
    }
}