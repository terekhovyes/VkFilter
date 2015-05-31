package me.alexeyterekhov.vkfilter.InternetNew.Requests

import me.alexeyterekhov.vkfilter.InternetNew.JSONParser
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation
import org.json.JSONObject

class RequestNotificationInfo(messageId: String) : Request("execute.notificationInfo") {
    init {
        params["message_id"] = messageId
    }

    override fun handleResponse(json: JSONObject) {
        val jsonNotification = json getJSONObject "response"
        val notification = JSONParser parseNotification jsonNotification
        GCMStation onLoadNotification notification
    }
}