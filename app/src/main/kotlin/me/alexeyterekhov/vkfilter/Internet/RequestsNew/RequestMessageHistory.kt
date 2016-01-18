package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message
import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.MissingDataAnalyzerNew
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestMessageHistory(
        val dialogId: DialogId,
        val count: Int,
        val olderThanId: String = ""
) : Request("messages.getHistory") {
    init {
        val firstMessageIsUseless = olderThanId != ""
        val correctedCount = count + (if (firstMessageIsUseless) 1 else 0)

        params["count"] = correctedCount + 1
        params[if (dialogId.isChat) "chat_id" else "user_id"] = dialogId.id
        if (olderThanId != "")
            params["start_message_id"] = olderThanId
    }

    override fun handleResponse(json: JSONObject) {
        val firstMessageIsUseless = olderThanId != ""

        val jsonMessageArray = json.getJSONObject("response").getJSONArray("items")
        val messages = JsonParserNew.parseMessages(jsonMessageArray)
        if (firstMessageIsUseless && messages.isNotEmpty())
            messages.removeAt(0)

        loadMissingUsers(messages)
        loadMissingVideos(messages)
        UpdateHandler.messages.saveMessageHistory(dialogId, messages, historyLoaded = messages.count() < count)
    }

    private fun loadMissingUsers(messages: Collection<Message>) {
        val missingIds = MissingDataAnalyzerNew.missingUsersIds(messages)
        if (missingIds.isNotEmpty())
            RequestControl.addBackground(RequestUsers(missingIds))
    }

    private fun loadMissingVideos(messages: Collection<Message>) {
        val missingIds = MissingDataAnalyzerNew.missingVideoIds(messages)
        if (missingIds.isNotEmpty())
            RequestControl.addBackground(RequestVideoUrls(dialogId, missingIds))
    }
}