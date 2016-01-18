package me.alexeyterekhov.vkfilter.Data.UpdateHandlers

import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.AttachedImage
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventAttachmentImageAdded
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventAttachmentImageProgress
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventAttachmentImageRemoved
import me.alexeyterekhov.vkfilter.Data.Utils.ImageUploadUtil
import me.alexeyterekhov.vkfilter.Util.EventBuses
import java.util.*

class AttachmentsHandler {
    fun addImageAttachment(dialogId: DialogId, image: AttachedImage) {
        val updatedImages = Vector(DialogCache.getDialogOrCreate(dialogId).current.attachedImages)
        updatedImages.add(image)
        DialogCache.getDialogOrCreate(dialogId).current.attachedImages = updatedImages
        EventBuses.dataBus().post(EventAttachmentImageAdded(dialogId, image))
        ImageUploadUtil.upload(dialogId, image)
    }

    fun progressImageAttachment(dialogId: DialogId, image: AttachedImage, percent: Int) {
        EventBuses.dataBus().post(EventAttachmentImageProgress(dialogId, image, percent))
    }

    fun removeImageAttachment(dialogId: DialogId, image: AttachedImage) {
        val updatedImages = Vector(DialogCache.getDialogOrCreate(dialogId).current.attachedImages)
        val index = updatedImages.indexOfFirst { it.filePath == image.filePath }
        if (index >= 0) {
            val removed = updatedImages.removeAt(index)
            DialogCache.getDialogOrCreate(dialogId).current.attachedImages = updatedImages
            EventBuses.dataBus().post(EventAttachmentImageRemoved(dialogId, image))
            ImageUploadUtil.cancel(removed)
        }
    }
}