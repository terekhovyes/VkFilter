package me.alexeyterekhov.vkfilter.Data.Entities.CurrentData

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.ImageAttachment
import me.alexeyterekhov.vkfilter.Internet.Upload.ControlOutputStream

class AttachedImage(
        val filePath: String,
        val dialogId: DialogId
) : LongUpload() {
    var imageUploadState: State = WaitingForUrl()

    public open class State
    public class WaitingForUrl : State()
    public class HasUploadUrl(val uploadUrl: String) : State()
    public class Uploading(val uploadUrl: String, val stream: ControlOutputStream) : State()
    public class Uploaded(val savedPhoto: String, var savedServer: Int, val savedHash: String): State()
    public class Saved(val id: String, val pid: String, val aid: String, val ownerId: String, val attachment: ImageAttachment): State() {
        fun attachmentId() = "photo${ownerId}_${id}"
    }
}