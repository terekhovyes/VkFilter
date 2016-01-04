package me.alexeyterekhov.vkfilter.DataCache.AttachedCache

import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import java.util.*

class Attached(val dialogId: String, val isChat: Boolean) {
    val listeners = LinkedList<DataDepend>()
    val images = AttachedImages(this)
    val messages = AttachedMessages(this)

    fun generateAttachmentsParam(): String {
        val images = images.uploads.map { it.generateAttachmentId() }
        return images.joinToString(separator = ",")
    }

    fun generateForwardMessagesParam(): String {
        val ids = LinkedList<Long>()
        messages.get().forEach {
            ids.addAll(it.messageIds)
        }
        return ids
                .distinct()
                .sorted()
                .joinToString(separator = ",")
    }
}