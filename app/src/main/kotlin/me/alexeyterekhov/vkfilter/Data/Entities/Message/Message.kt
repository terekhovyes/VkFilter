package me.alexeyterekhov.vkfilter.Data.Entities.Message

class Message(val senderId: String) : Cloneable {
    var sent = SentState()
    var isRead = false
    var isNotRead: Boolean
        get() = !isRead
        set(value) { isRead = !value }
    var isOut = false
    var isIn: Boolean
        get() = !isOut
        set(value) { isIn = !value }
    var data = MessageData()

    override public fun clone(): Any {
        val copy = Message(senderId)

        copy.sent = sent.clone() as SentState
        copy.isRead = isRead
        copy.isOut = isOut
        copy.data = data.clone() as MessageData

        return copy
    }
}