package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.AttachedImage
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Utils.ImageUploadUtil
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestMessagePhotoServer(val dialogId: DialogId, val image: AttachedImage) : Request("photos.getMessagesUploadServer") {
    override fun handleResponse(json: JSONObject) {
        val uploadUrl = json.getJSONObject("response").getString("upload_url")
        image.imageUploadState = AttachedImage.HasUploadUrl(uploadUrl)
        ImageUploadUtil.upload(dialogId, image)
    }
}