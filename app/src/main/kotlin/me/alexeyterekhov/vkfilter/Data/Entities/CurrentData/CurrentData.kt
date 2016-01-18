package me.alexeyterekhov.vkfilter.Data.Entities.CurrentData

import java.util.*

class CurrentData {
    var typingText = ""
    var attachedMessages: AttachedMessages? = null
    var attachedImages: List<AttachedImage> = LinkedList()
    var sendMessageAfterImageUploading = false

    fun generateAttachmentsParam(): String {
        val images = attachedImages
                .filter { it.imageUploadState is AttachedImage.Saved }
                .map { (it.imageUploadState as AttachedImage.Saved).attachmentId() }

        return images.joinToString(separator = ",")
    }

    fun generateForwardMessagesParam(): String {
        if (attachedMessages == null || attachedMessages!!.messageIds.isEmpty())
            return ""
        return attachedMessages!!.messageIds.sorted().joinToString(separator = ",")
    }
}