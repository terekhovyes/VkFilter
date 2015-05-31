package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.Internet.JSONParser
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