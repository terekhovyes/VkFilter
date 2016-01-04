package me.alexeyterekhov.vkfilter.DataClasses

import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.ImageAttachment
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestMessagePhotoServer
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestSavePhoto
import me.alexeyterekhov.vkfilter.Internet.Upload.ControlOutputStream
import me.alexeyterekhov.vkfilter.Internet.Upload.UploadImageRecipe
import me.alexeyterekhov.vkfilter.Util.Chef
import org.json.JSONObject

class ImageUpload(
        val filePath: String,
        val dialogId: String,
        val isChat: Boolean
) {
    companion object {
        val STATE_WAIT = 1
        val STATE_IN_PROCESS = 2
        val STATE_UPLOADED = 3
    }

    private var canceled = false
    private var controlStream: ControlOutputStream? = null
    var state = STATE_WAIT
    var loadedPercent = 0 // in percents 0-100

    // Server response data
    var uploadUrl = ""
    private var id = ""
    private var pid = ""
    private var aid = ""
    private var ownerId = ""
    var attachment: ImageAttachment? = null

    var savedPhotoParam = ""
    var savedServerParam = 0
    var savedHashParam = ""


    fun startUploading() {
        RequestControl addBackground RequestMessagePhotoServer(this)
        state = STATE_IN_PROCESS
    }
    fun cancelUploading() {
        canceled = true
        state = STATE_WAIT
        controlStream?.cancelStreaming()
    }
    fun isCanceled() = canceled

    fun generateAttachmentId() = "photo${ownerId}_${id}"

    // Uploading methods
    fun onGetUploadUrl(url: String) {
        if (canceled)
            return
        uploadUrl = url
        Chef.cook(UploadImageRecipe.recipe, this)
    }
    fun onStartUploading(stream: ControlOutputStream) {
        if (canceled)
            stream.cancelStreaming()
        else
            controlStream = stream
    }
    fun onProgress(percent: Int) {
        loadedPercent = percent
        AttachedCache
                .get(dialogId, isChat)
                .images
                .onProgress(filePath, percent)
    }
    fun onFinishUploading(saveString: String) {
        if (canceled)
            return
        val json = JSONObject(saveString)
        savedPhotoParam = json.getString("photo")
        savedServerParam = json.getInt("server")
        savedHashParam = json.getString("hash")
        RequestControl addBackground RequestSavePhoto(this)

    }
    fun onSaveOnServer(
            id: String,
            ownerId: String,
            pid: String = "",
            aid: String = ""
    ) {
        this.id = id
        this.pid = pid
        this.ownerId = ownerId
        this.aid = aid
        state = STATE_UPLOADED

        AttachedCache
                .get(dialogId, isChat)
                .images
                .onFinish(filePath)
    }
}