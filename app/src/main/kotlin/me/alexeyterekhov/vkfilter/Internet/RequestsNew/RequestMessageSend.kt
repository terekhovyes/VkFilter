package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import android.text.TextUtils
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.CurrentData
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject

class RequestMessageSend(
        val dialogId: DialogId,
        val currentData: CurrentData
) : Request("messages.send") {
    init {
        val guid = System.currentTimeMillis()
        val attachments = currentData.generateAttachmentsParam()
        val forwardMessages = currentData.generateForwardMessagesParam()

        params["message"] = currentData.typingText
        params[if (dialogId.isChat) "chat_id" else "user_id"] = dialogId.id
        params["guid"] = System.currentTimeMillis()
        if (!TextUtils.isEmpty(attachments))
            params["attachment"] = attachments
        if (!TextUtils.isEmpty(forwardMessages))
            params["forward_messages"] = forwardMessages

        UpdateHandler.messages.sendMessage(dialogId, guid, currentData)
    }

    override fun handleResponse(json: JSONObject) {
        val sentId = json.getLong("response")
        val guid = getSendingGuid()
        UpdateHandler.messages.messageSent(dialogId, guid, sentId)
    }

    fun getSendingGuid() = params["guid"] as Long
}