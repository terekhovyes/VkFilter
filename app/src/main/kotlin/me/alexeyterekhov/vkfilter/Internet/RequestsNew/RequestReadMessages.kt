package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestReadMessages(val dialogId: DialogId) : Request("messages.markAsRead") {
    private var lastReadMessageId = 0L
    private var allowExecuteRequest = true

    init {
        val notReadIncomeMessages = DialogCache.getDialogOrCreate(dialogId)
                .messages
                .history
                .filter { it.isIn && it.isNotRead }
        if (notReadIncomeMessages.isEmpty()) {
            allowExecuteRequest = false
        } else {
            params["message_ids"] = notReadIncomeMessages
                    .map { it.sent.id.toString() }
                    .joinToString(separator = ",")
            lastReadMessageId = (notReadIncomeMessages.maxBy { it.sent.id })!!.sent.id;
        }
    }

    override fun allowExecuteRequest() = allowExecuteRequest

    override fun handleResponse(json: JSONObject) {
        val response = json.getInt("response")
        if (response == 1)
            UpdateHandler.messages.readMessages(DialogCache.getDialogOrCreate(dialogId), out = false, lastId = lastReadMessageId)
    }
}