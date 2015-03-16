package me.alexeyterekhov.vkfilter.DataClasses

import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments

class Message(val sender: User) {
    public var id: Long = 0
    public var isRead: Boolean = false
    public var isOut: Boolean = false
    public var text: String = ""
    public var formattedDate: String = ""
    public var dateMSC: Long = 0
    public val attachments: Attachments = Attachments()
}