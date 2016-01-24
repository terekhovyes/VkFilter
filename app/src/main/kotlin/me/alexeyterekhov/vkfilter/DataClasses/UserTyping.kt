package me.alexeyterekhov.vkfilter.DataClasses

class UserTyping() {
    constructor(time: Long, id: String) : this() {
        userId = id
        eventTimeMillis = time
    }

    var userId = ""
    var eventTimeMillis = 0L
}