package me.alexeyterekhov.vkfilter.Internet.Events

class EventMessageRead : EventBase() {
    var lastMessageId = 0L
    var incomes = false
    var dialogId = 0L
    var isChat = false
}