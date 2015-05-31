package me.alexeyterekhov.vkfilter.Internet

import android.os.Handler
import me.alexeyterekhov.vkfilter.DataCache.*
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.VideoAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageForSending
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestBundle
import me.alexeyterekhov.vkfilter.InternetNew.DialogRefresher
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation
import org.json.JSONObject
import java.util.Collections
import java.util.LinkedList
import java.util.Vector


object ResponseHandler {
    public fun handle(request: VkRequestBundle, result: JSONObject) {
        when (request.vkFun) {
            VkFun.dialogList -> dialogList(request, result)
            VkFun.friendList -> friendList(request, result)
            VkFun.messageList -> messageList(request, result)
            VkFun.userInfo -> userInfo(request, result)
            VkFun.chatInfo -> chatInfo(request, result)
            VkFun.markIncomesAsReadOld -> markIncomesAsReadOld(request, result)
            VkFun.markIncomesAsRead -> markIncomesAsRead(request, result)
            VkFun.refreshDialog -> refreshDialog(request, result)
            VkFun.sendMessageOld -> sendMessageOld(request, result)
            VkFun.sendMessage -> sendMessage(request, result)
            VkFun.notificationInfo -> notificationInfo(request, result)
            VkFun.getDialogPartners -> getDialogPartners(request, result)
            VkFun.videoUrls -> videoUrls(request, result)
        }
    }

    private fun dialogList(request: VkRequestBundle, result: JSONObject) {
        val users = Vector<User>()

        val offset = request.vkParams["offset"] as Int
        val jsonUsers = JSONParser detailedDialogsResponseToUserList result
        val jsonDialogs = JSONParser detailedDialogsResponseToDialogList result

        users addAll (JSONParser parseUsers jsonUsers)
        for (u in users)
            UserCache.putUser(u)

        val dialogs = JSONParser parseDialogs jsonDialogs
        val prevSnap = DialogListCache.getSnapshot()
        val mergedList = Vector<Dialog>()
        mergedList addAll prevSnap.dialogs.subList(0, offset)
        mergedList addAll dialogs
        val newSnap = DialogListSnapshot(System.currentTimeMillis(), mergedList)

        DialogListCache updateSnapshot newSnap
        UserCache.dataUpdated()
    }

    private fun friendList(request: VkRequestBundle, result: JSONObject) {
        val offset = request.vkParams["offset"] as Int
        val jsonUsers = JSONParser friendListResponseToUserList result

        val friends = JSONParser parseUsers jsonUsers
        for (u in friends)
            UserCache.putUser(u)
        if (offset == 0)
            FriendsListCache reloadList friends
        else
            FriendsListCache addItems friends
    }

    private fun messageList(request: VkRequestBundle, result: JSONObject) {
        val p = request.vkParams

        val count = p["count"] as Int
        val isChat = p contains "chat_id"
        val id =
                if (!isChat)
                    p["user_id"] as String
                else
                    p["chat_id"] as String
        val startMessageId =
                if (p contains "start_message_id")
                    p["start_message_id"] as String
                else ""
        val offset =
                if (p contains "offset") p["offset"] as Int
                else 0
        val firstMessageIsUseless = startMessageId != "" && offset == 0

        val jsonMessages = JSONParser messageListResponseToMessageList result
        val messages = JSONParser parseMessages jsonMessages
        if (firstMessageIsUseless && messages.isNotEmpty())
            messages.remove(0)

        val originalCount = count - (if (firstMessageIsUseless) 1 else 0)
        loadNotLoadedUsers(Vector(messages map { it.toNewFormat() }))
        loadNotLoadedVideos(id, isChat, Vector(messages map { it.toNewFormat() }))
        MessageCaches.getCache(id, isChat).putMessages(
                messages = messages map { it.toNewFormat() },
                allHistoryLoaded = messages.count() < originalCount
        )
        MessageCacheOld
                .getDialog(id, isChat)
                .addMessagesWithReplace(messages, messages.size() < originalCount)
    }

    private fun loadNotLoadedUsers(messages: Vector<Message>) {
        fun notLoadedUsers(m: Message): LinkedList<String> {
            val list = if (m.attachments.messages.isNotEmpty())
                m.attachments.messages
                        .map { notLoadedUsers(it) }
                        .foldRight(LinkedList<String>(), {
                            list, el ->
                            list addAll el
                            list
                        })
            else
                LinkedList<String>()

            if (!UserCache.contains(m.senderId))
                list add m.senderId
            return list
        }
        val notLoadedIds = messages
                .map { notLoadedUsers(it) }
                .foldRight(LinkedList<String>(), {
                    list, el ->
                    list addAll el
                    list
                })
        if (notLoadedIds.isNotEmpty())
            RunFun userInfo notLoadedIds
    }

    private fun loadNotLoadedVideos(dialogId: String, isChat: Boolean, messages: Vector<Message>) {
        fun notLoadedIds(m: Message): LinkedList<String> {
            val list = if (m.attachments.messages.isNotEmpty())
                m.attachments.messages
                        .map { notLoadedIds(it) }
                        .foldRight(LinkedList<String>(), {
                            list, el ->
                            list addAll el
                            list
                        })
            else
                LinkedList<String>()
            m.attachments.videos filter { it.playerUrl == "" } forEach {
                list add it.requestKey
            }
            return list
        }
        val notLoadedIds = messages
                .map { notLoadedIds(it) }
                .foldRight(LinkedList<String>(), {
                    list, el ->
                    list addAll el
                    list
                })
        if (notLoadedIds.isNotEmpty())
            RunFun.getVideoUrls(dialogId, isChat, notLoadedIds)
    }

    private fun userInfo(request: VkRequestBundle, result: JSONObject) {
        val jsonUsers = JSONParser userInfoResponseToUserList result
        val users = JSONParser parseUsers jsonUsers
        for (u in users)
            UserCache.putUser(u)
        UserCache.dataUpdated()
    }

    private fun chatInfo(request: VkRequestBundle, result: JSONObject) {
        val jsonUsers = JSONParser chatInfoResponseToUserList result
        val users = JSONParser parseUsers jsonUsers
        users forEach { UserCache.putUser(it) }
        UserCache.dataUpdated()

        val jsonChats = JSONParser chatInfoResponseToChatList result
        val chats = JSONParser parseChatInfo jsonChats
        chats forEach { ChatInfoCache.putChat(it.id.toString(), it) }
        ChatInfoCache.dataUpdated()
    }

    private fun markIncomesAsReadOld(request: VkRequestBundle, result: JSONObject) {
        val response = result.getInt("response")
        val id = request.additionalParams.getString("id")
        val chat = request.additionalParams.getBoolean("chat")
        if (response == 1) {
            MessageCacheOld.getDialog(id, chat).markIncomesAsRead()
        } else {
            Handler().postDelayed({
                RunFun.markIncomesAsReadOld(id, chat)
            }, 1000)
        }
    }

    private fun markIncomesAsRead(request: VkRequestBundle, result: JSONObject) {
        val response = result.getInt("response")
        val dialogId = request.additionalParams.getString("dialogId")
        val isChat = request.additionalParams.getBoolean("isChat")
        val lastReadId = request.additionalParams.getLong("lastReadId")
        if (response == 1) {
            MessageCaches.getCache(dialogId, isChat).onReadMessages(false, lastReadId)
        } else {
            Handler().postDelayed({
                RunFun.markIncomesAsRead(dialogId, isChat)
            }, 1000)
        }
    }

    private fun refreshDialog(request: VkRequestBundle, result: JSONObject) {
        if (result.isNull("response")) {
            DialogRefresherOld.onDataUpdate()
            DialogRefresher.messageCacheListener.onAddNewMessages(0)
        } else {
            val p = request.vkParams

            val chat = p contains "chat_id"
            val id = if (!chat)
                         p["user_id"] as String
                     else
                         p["chat_id"] as String

            val response = result.getJSONObject("response")
            if (response.has("read")) {
                val lastReadId = response.getLong("read")
                MessageCacheOld.getDialog(id, chat).markOutcomesAsRead(lastReadId)
                MessageCaches.getCache(id, chat).onReadMessages(out = true, lastId = lastReadId)
            }
            if (response.has("new_messages")) {
                val jsonMessages = response.getJSONArray("new_messages")
                val messages = JSONParser parseMessages jsonMessages
                MessageCacheOld.getDialog(id, chat).addMessagesWithReplace(messages, false)
                if (messages.isNotEmpty()) {
                    MessageCaches.getCache(id, chat).putMessages(
                            messages = messages map { it.toNewFormat() },
                            allHistoryLoaded = false)
                } else {
                    DialogRefresher.messageCacheListener.onAddNewMessages(0)
                }
            }
        }
    }

    private fun sendMessageOld(request: VkRequestBundle, result: JSONObject) {
        val p = request.vkParams

        val sentMessageId = result.getLong("response")
        val chat = p contains "chat_id"
        val id = if (!chat)
                     p["user_id"] as String
                 else
                     p["chat_id"] as String

        val outMessage = MessageForSending()
        with (outMessage) {
            dialogId = id
            isChat = chat
            text = p["message"] as String
        }
        val sentMessage = outMessage.transformToMessage(sentMessageId)
        MessageCacheOld
                .getDialog(id, chat)
                .addMessagesWithReplace(
                    msgs = Collections.singleton(sentMessage),
                    itsAll = false
                )
    }

    private fun sendMessage(request: VkRequestBundle, result: JSONObject) {
        val params = request.vkParams
        val sentId = result.getLong("response")
        val isChat = params contains "chat_id"
        val dialogId = if (isChat)
            params["chat_id"] as String
        else
            params["user_id"] as String
        val guid = params["guid"] as Long
        MessageCaches.getCache(dialogId, isChat).onDidSendMessage(guid, sentId)
    }

    private fun notificationInfo(request: VkRequestBundle, result: JSONObject) {
        val info = JSONParser parseNotificationInfo (JSONParser notificationInfoToObject result)
        GCMStation onLoadNotification info
    }

    private fun getDialogPartners(request: VkRequestBundle, result: JSONObject) {
        val jsonUsers = JSONParser dialogPartnersToArray result
        val users = JSONParser parseUsers jsonUsers
        users forEach { UserCache.putUser(it) }
    }

    private fun videoUrls(request: VkRequestBundle, result: JSONObject) {
        val dialogId = request.additionalParams.getString("id")
        val isChat = request.additionalParams.getBoolean("chat")

        val gotUrls = JSONParser parseVideoUrls (JSONParser videoUrlsResponseToArray result)
        val dialog = MessageCaches.getCache(dialogId, isChat)

        fun findAttachments(vid: Long, m: Message): LinkedList<VideoAttachment> {
            val list = if (m.attachments.messages.isNotEmpty())
                m.attachments.messages
                        .map { findAttachments(vid, it) }
                        .foldRight(LinkedList<VideoAttachment>(), {
                            list, el ->
                            list addAll el
                            list
                        })
            else
                LinkedList<VideoAttachment>()
            m.attachments.videos filter { it.id == vid } forEach {
                list add it
            }
            return list
        }


        gotUrls forEach {
            val id = it.id
            val url = it.playerUrl

            dialog.getMessages()
                    .map { findAttachments(id, it) }
                    .map { it forEach { it.playerUrl = url } }
        }
        //MessageCaches.getCache(dialogId, isChat).onDataUpdate()
    }
}