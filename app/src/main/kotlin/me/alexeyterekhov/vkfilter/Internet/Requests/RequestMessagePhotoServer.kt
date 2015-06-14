package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import org.json.JSONObject

class RequestMessagePhotoServer(
        val imageUpload: ImageUpload
) : Request("photos.getMessagesUploadServer") {
    override fun handleResponse(json: JSONObject) {
        val uploadUrl = json getJSONObject "response" getString "upload_url"
        imageUpload.onGetUploadUrl(uploadUrl)
    }
}