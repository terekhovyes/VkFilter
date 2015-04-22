package me.alexeyterekhov.vkfilter.DataClasses

import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments
import java.util.LinkedList

class Message(val senderId: String) {
    var id = 0L
    // State
    var isRead = false
    var isOut = false
    // Date
    var formattedDate = ""
    var dateMSC = 0L
    // Data
    var text = ""
    val attachments = Attachments()
    val forwardMessages = LinkedList<Message>()

    fun senderOrEmpty(): User = UserCache.getUser(senderId) ?: User()
}