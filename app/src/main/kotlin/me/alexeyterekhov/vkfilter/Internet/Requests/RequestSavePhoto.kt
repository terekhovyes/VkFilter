package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import org.json.JSONObject

class RequestSavePhoto(val upload: ImageUpload) : Request("photos.saveMessagesPhoto") {
    init {
        params["photo"] = upload.savedPhotoParam
        params["server"] = upload.savedServerParam
        params["hash"] = upload.savedHashParam
    }

    override fun handleResponse(json: JSONObject) {
        val response = json getJSONArray "response" getJSONObject 0
        upload.onSaveOnServer(
                response.getString("id"),
                response.getString("owner_id")
        )
    }
}