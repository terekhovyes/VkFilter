package me.alexeyterekhov.vkfilter.DataClasses

import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments

class Message(val senderId: String) {
    public var id: Long = 0
    public var isRead: Boolean = false
    public var isOut: Boolean = false
    public var text: String = ""
    public var formattedDate: String = ""
    public var dateMSC: Long = 0
    public val attachments: Attachments = Attachments()

    public fun senderOrEmpty(): User = UserCache.getUser(senderId) ?: User()
}