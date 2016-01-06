package me.alexeyterekhov.vkfilter.Internet.Events

class EventUserTyping : EventBase() {
    var userId = ""
    var dialogId = 0L
    var isChat = false
}