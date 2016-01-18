package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.AttachedImage
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.LongUpload
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Utils.ImageUploadUtil
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestSavePhoto(val dialogId: DialogId, val upload: AttachedImage) : Request("photos.saveMessagesPhoto") {
    init {
        val state = (upload.imageUploadState as AttachedImage.Uploaded)
        params["photo"] = state.savedPhoto
        params["server"] = state.savedServer
        params["hash"] = state.savedHash
    }

    override fun handleResponse(json: JSONObject) {
        val response = (json.getJSONArray("response")).getJSONObject(0)
        val attachment = JsonParserNew.parseImageAttachment(json)
        upload.imageUploadState = AttachedImage.Saved(
                id = response.getString("id"),
                pid = "",
                aid = "",
                ownerId = response.getString("owner_id"),
                attachment = attachment)
        upload.state = LongUpload.UploadState.UPLOADED
        ImageUploadUtil.upload(dialogId, upload)
    }
}