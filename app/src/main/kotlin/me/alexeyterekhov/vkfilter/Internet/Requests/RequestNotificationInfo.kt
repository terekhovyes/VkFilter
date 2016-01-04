package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.Internet.JSONParser
import me.alexeyterekhov.vkfilter.NotificationService.DataHandling.IntentHandler
import org.json.JSONObject

class RequestNotificationInfo(messageId: String) : Request("execute.notificationInfo") {
    init {
        params["message_id"] = messageId
    }

    override fun handleResponse(json: JSONObject) {
        val jsonNotification = json.getJSONObject("response")
        val notification = JSONParser parseNotification jsonNotification
        IntentHandler.onLoadNotification(notification)
    }
}