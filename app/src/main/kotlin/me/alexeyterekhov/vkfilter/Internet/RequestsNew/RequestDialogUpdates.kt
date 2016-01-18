package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message
import me.alexeyterekhov.vkfilter.Data.UpdateEvents.EventRefresherGetResponse
import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.MissingDataAnalyzerNew
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import me.alexeyterekhov.vkfilter.Util.EventBuses
import org.json.JSONObject

class RequestDialogUpdates(val dialogId: DialogId) : Request("execute.dialogUpdates") {
    init {
        params[if (dialogId.isChat) "chat_id" else "user_id"] = dialogId.id
        params["last_id"] = DialogCache.getDialog(dialogId)!!.messages.lastMessageIdFromServer
        val lastOutReadMessage = DialogCache.getDialog(dialogId)!!
                .messages
                .history
                .lastOrNull { it.isOut && it.isRead }
        params["read_id"] = lastOutReadMessage?.sent?.id ?: 0L
    }

    override fun handleResponse(json: JSONObject) {
        if (!json.isNull("response")) {
            val response = json.getJSONObject("response")

            if (response.has("read")) {
                val lastReadId = response.getLong("read")
                UpdateHandler.messages.readMessages(DialogCache.getDialog(dialogId)!!, out = true, lastId = lastReadId)
            }

            if (response.has("new_messages")) {
                val jsonMessageArray = response.getJSONArray("new_messages")
                val newMessages = JsonParserNew.parseMessages(jsonMessageArray)

                if (newMessages.isNotEmpty()) {
                    UpdateHandler.messages.saveMessageHistory(dialogId, newMessages, historyLoaded = false)
                    loadMissingUsers(newMessages)
                    loadMissingVideos(newMessages)
                }
            }
        }
        EventBuses.dataBus().post(EventRefresherGetResponse())
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