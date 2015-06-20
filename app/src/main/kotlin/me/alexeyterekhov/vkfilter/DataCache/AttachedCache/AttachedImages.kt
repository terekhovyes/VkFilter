package me.alexeyterekhov.vkfilter.DataCache.AttachedCache

import me.alexeyterekhov.vkfilter.DataCache.Common.forEachSync
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.RequestModule
import java.util.LinkedList

class AttachedImages(val dialogId: String, val isChat: Boolean) {
    val uploads = LinkedList<ImageUpload>()
    val listeners = LinkedList<AttachedImageListener>()
    var sendMessageAfterUploading = false

    fun putImage(imagePath: String) {
        val a = ImageUpload(imagePath, dialogId, isChat)
        uploads add a
        listeners forEachSync { it.onAdd(a) }
        keepUploading()
    }
    fun removeImage(imagePath: String) {
        val ind = uploads indexOfFirst { it.filePath == imagePath }
        val a = uploads remove ind
        listeners forEachSync { it.onRemoved(a) }
        a.cancelUploading()
        keepUploading()
    }

    fun onProgress(imagePath: String, percent: Int) {
        val img = getByPath(imagePath)
        if (img != null)
            listeners forEach { it.onProgress(img, percent) }
    }
    fun onFinish(imagePath: String) {
        val img = getByPath(imagePath)
        if (img != null)
            listeners forEach { it.onFinish(img) }
        keepUploading()
    }

    fun getUploaded() = uploads filter { it.state == ImageUpload.STATE_UPLOADED }
    fun removeUploaded() {
        val uploaded = getUploaded()
        uploaded forEach { up ->
            uploads remove up
            listeners forEachSync { it.onRemoved(up) }
        }
    }

    fun getByPath(path: String) = uploads firstOrNull { it.filePath == path }

    private fun keepUploading() {
        if (uploads none { it.state == ImageUpload.STATE_IN_PROCESS }) {
            uploads.firstOrNull { it.state == ImageUpload.STATE_WAIT }
                    ?.startUploading()
        }
        if (sendMessageAfterUploading && uploads all { it.state == ImageUpload.STATE_UPLOADED }) {
            sendMessageAfterUploading = false
            val editMessage = MessageCaches.getCache(dialogId, isChat).getEditMessage()
            RequestModule.sendMessage(editMessage, dialogId, isChat)
        }
    }

    interface AttachedImageListener {
        fun onAdd(image: ImageUpload) {}
        fun onRemoved(image: ImageUpload) {}
        fun onProgress(image: ImageUpload, percent: Int) {}
        fun onFinish(image: ImageUpload) {}
    }
}