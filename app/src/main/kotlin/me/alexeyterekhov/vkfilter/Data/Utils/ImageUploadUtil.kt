package me.alexeyterekhov.vkfilter.Data.Utils

import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.AttachedImage
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.LongUpload
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventAttachmentImageUploaded
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.RequestsNew.RequestMessagePhotoServer
import me.alexeyterekhov.vkfilter.Internet.RequestsNew.RequestMessageSend
import me.alexeyterekhov.vkfilter.Internet.RequestsNew.RequestSavePhoto
import me.alexeyterekhov.vkfilter.Internet.Upload.UploadImageRecipeNew
import me.alexeyterekhov.vkfilter.Util.Chef
import me.alexeyterekhov.vkfilter.Util.EventBuses
import java.util.*

object ImageUploadUtil {
    private val uploads = LinkedList<Pair<DialogId, AttachedImage>>()

    fun upload(dialogId: DialogId, image: AttachedImage) {
        if (uploads.none { it.second.filePath == image.filePath }) {
            uploads.add(Pair(dialogId, image))
            keepUploading()
        } else {
            when (image.imageUploadState) {
                is AttachedImage.WaitingForUrl -> {
                    image.state = LongUpload.UploadState.IN_PROCESS
                    RequestControl.addBackground(RequestMessagePhotoServer(dialogId, image))
                }
                is AttachedImage.HasUploadUrl -> {
                    if (!image.canceled) {
                        Chef.cook(UploadImageRecipeNew.recipe, Pair(dialogId, image))
                    }
                }
                is AttachedImage.Uploaded -> {
                    if (!image.canceled)
                        RequestControl.addBackground(RequestSavePhoto(dialogId, image))
                }
                is AttachedImage.Saved -> {
                    EventBuses.dataBus().post(EventAttachmentImageUploaded(dialogId, image))
                    uploads.removeAll { it.second == image }
                    keepUploading()
                    checkMessageAutosend(dialogId)
                }
            }
        }
    }

    fun cancel(image: AttachedImage) {
        image.canceled = true
        image.state = LongUpload.UploadState.WAIT
        when (image.imageUploadState) {
            is AttachedImage.Uploading -> {
                (image.imageUploadState as AttachedImage.Uploading).stream.cancelStreaming()
            }
        }
        uploads.removeAll { it.second == image }
        keepUploading()
    }

    private fun keepUploading() {
        if (uploads.none { it.second.state == LongUpload.UploadState.IN_PROCESS }) {
            val next = uploads.firstOrNull { it.second.state == LongUpload.UploadState.WAIT }
            if (next != null) {
                next.second.state = LongUpload.UploadState.IN_PROCESS
                next.second.imageUploadState = AttachedImage.WaitingForUrl()
                upload(next.first, next.second)
            }
        }
    }

    private fun checkMessageAutosend(dialogId: DialogId) {
        val dialog = DialogCache.getDialogOrCreate(dialogId)
        if (dialog.current.sendMessageAfterImageUploading) {
            if (uploads.none { it.first == dialogId }) {
                RequestControl.addBackgroundOrdered(RequestMessageSend(dialogId, dialog.current))
            }
        }
    }
}

/*
class AttachedImages(val attached: Attached) {
    val uploads = LinkedList<ImageUpload>()
    val listeners = LinkedList<AttachedImageListener>()
    var sendMessageAfterUploading = false

    fun onProgress(imagePath: String, percent: Int) {
        val img = getByPath(imagePath)
        if (img != null)
            listeners.forEach { it.onProgress(img, percent) }
    }

    fun getUploaded() = uploads.filter { it.state == ImageUpload.STATE_UPLOADED }
    fun removeUploaded() {
        val uploaded = getUploaded()
        uploaded.forEach { up ->
            uploads.remove(up)
            listeners forEachSync { it.onRemoved(up) }
        }
    }

    fun getByPath(path: String) = uploads.firstOrNull { it.filePath == path }
}
 */
