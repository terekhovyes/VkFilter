package me.alexeyterekhov.vkfilter.Internet.VkApi

import android.os.Bundle
import com.vk.sdk.api.VKParameters
import me.alexeyterekhov.vkfilter.DataCache.MessageCache
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageForSending

public object RunFun {
    public fun dialogList(offset: Int, count: Int) {
        val params = VKParameters()
        params["offset"] = offset
        params["count"] = count
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.dialogList, params))
    }

    public fun messageList(
            dialogId: String,
            dialogIsChat: Boolean,
            offset: Int,
            count: Int,
            startMessageId: String = ""
    ) {
        val firstMessageIsUseless = startMessageId != "" && offset == 0
        val correctedCount = count + (if (firstMessageIsUseless) 1 else 0)

        val params = VKParameters()
        params["count"] = correctedCount + 1
        params[if (dialogIsChat) "chat_id" else "user_id"] = dialogId
        if (offset != 0) params["offset"] = offset
        if (startMessageId != "") params["start_message_id"] = startMessageId
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.messageList, params))
    }

    public fun friendList(offset: Int, count: Int) {
        val params = VKParameters()
        params["count"] = count
        params["offset"] = offset
        params["order"] = "hints"
        params["fields"] = "name,sex,photo_max,last_seen"
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.friendList, params))
    }

    public fun userInfo(ids: Collection<String>) {
        val params = VKParameters()
        params["user_ids"] = ids.join(separator = ",")
        params["fields"] = "name,sex,online,photo_max,last_seen"
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.userInfo, params))
    }

    public fun chatInfo(ids: Collection<String>) {
        val params = VKParameters()
        params["chat_ids"] = ids.join(separator = ",")
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.chatInfo, params))
    }

    public fun sendMessage(msg: MessageForSending) {
        val params = VKParameters()
        params["message"] = msg.text
        params[if (msg.isChat) "chat_id" else "user_id"] = msg.dialogId
        params["guid"] = System.currentTimeMillis()
        VkRequestControl.addOrderImportantRequest(VkRequestBundle(VkFun.sendMessage, params))
    }

    public fun markIncomesAsRead(id: String, chat: Boolean) {
        val messagePack = MessageCache.getDialog(id, chat)
        if (messagePack.messages any {!it.isOut && !it.isRead}) {
            val ids = messagePack.messages filter { !it.isOut && !it.isRead } map { it.id.toString() }
            val params = VKParameters()
            params["message_ids"] = ids.joinToString(separator = ",")
            val additionalParams = Bundle()
            additionalParams.putString("id", id)
            additionalParams.putBoolean("chat", chat)
            VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.markIncomesAsRead, params, additionalParams))
        }
    }

    public fun registerGCM(id: String) {
        val params = VKParameters()
        params["token"] = id
        params["subscribe"] = "msg"
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.registerGCM, params))
    }

    public fun unregisterGCM(id: String) {
        val params = VKParameters()
        params["token"] = id
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.unregisterGCM, params))
    }

    public fun notificationInfo(messageId: String) {
        val params = VKParameters()
        params["message_id"] = messageId
        VkRequestControl.addUnstoppableRequest(VkRequestBundle(VkFun.notificationInfo, params))
    }

    public fun getDialogPartners(id: Long, isChat: Boolean) {
        val params = VKParameters()
        params["id"] = id
        params["chat"] = if (isChat) 1 else 0
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.getDialogPartners, params))
    }

    public fun getVideoUrls(dialogId: String, isChat: Boolean, ids: Collection<String>) {
        val params = VKParameters()
        params["video_ids"] = "'${ids.joinToString(separator = ",")}'"
        val additional = Bundle()
        additional.putString("id", dialogId)
        additional.putBoolean("chat", isChat)
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.videoUrls, params, additional))
    }
}