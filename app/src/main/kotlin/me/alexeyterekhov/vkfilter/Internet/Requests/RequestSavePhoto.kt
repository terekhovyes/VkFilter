package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import me.alexeyterekhov.vkfilter.Internet.JSONParser
import org.json.JSONObject

class RequestSavePhoto(val upload: ImageUpload) : Request("photos.saveMessagesPhoto") {
    init {
        params["photo"] = upload.savedPhotoParam
        params["server"] = upload.savedServerParam
        params["hash"] = upload.savedHashParam
    }

    override fun handleResponse(json: JSONObject) {
        val response = (json.getJSONArray("response")).getJSONObject(0)
        val attachment = JSONParser parseImageAttachment json
        upload.attachment = attachment
        upload.onSaveOnServer(
                response.getString("id"),
                response.getString("owner_id")
        )
    }
}