package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import me.alexeyterekhov.vkfilter.DataClasses.MessageOld

class MessageForSending {
    var dialogId = ""
    var isChat = false

    // message content
    var text = ""

    fun transformToMessage(messageId: Long): MessageOld {
        val msg = MessageOld("me")
        with (msg) {
            id = messageId
            text = this@MessageForSending.text
            dateMSC = System.currentTimeMillis()
            isOut = true
            isRead = false
        }
        return msg
    }
}