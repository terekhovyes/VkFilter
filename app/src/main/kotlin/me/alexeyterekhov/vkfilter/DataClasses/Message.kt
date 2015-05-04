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
}