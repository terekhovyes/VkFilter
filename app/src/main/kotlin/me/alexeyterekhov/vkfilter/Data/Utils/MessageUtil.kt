package me.alexeyterekhov.vkfilter.Data.Utils

import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.AttachedImage
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.CurrentData
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message
import me.alexeyterekhov.vkfilter.Data.Entities.Message.MessageData

object MessageUtil {
    fun messagesEquals(m1: Message, m2: Message): Boolean {
        return m1.sent.state == m2.sent.state
                && m1.sent.id == m2.sent.id
                && m1.sent.timeMillis == m2.sent.timeMillis
                && m1.data.text == m2.data.text
                && m1.isRead == m2.isRead
    }

    fun currentDataToMessageData(currentData: CurrentData): MessageData {
        val d = MessageData()

        d.text = currentData.typingText
        d.images = currentData.attachedImages
                .filter { it.imageUploadState is AttachedImage.Saved }
                .map { (it.imageUploadState as AttachedImage.Saved).attachment }
        if (currentData.attachedMessages != null)
            d.messages = DialogCache.getDialog(currentData.attachedMessages!!.dialogId)!!
                    .messages
                    .history
                    .filter { currentData.attachedMessages!!.messageIds.contains(it.sent.id) }

        return d
    }
}