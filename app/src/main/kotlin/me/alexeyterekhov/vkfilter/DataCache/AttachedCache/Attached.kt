package me.alexeyterekhov.vkfilter.DataCache.AttachedCache

class Attached(val dialogId: String, val isChat: Boolean) {
    val images = AttachedImages(dialogId, isChat)

    fun generateAttachmentsParam(): String {
        val images = images.uploads map { it.generateAttachmentId() }
        return images.joinToString(separator = ",")
    }
}