package me.alexeyterekhov.vkfilter.Internet

import android.os.AsyncTask
import android.os.Handler
import me.alexeyterekhov.vkfilter.DataCache.*
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageForSending
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkFun
import me.alexeyterekhov.vkfilter.Internet.VkApi.VkRequestBundle
import me.alexeyterekhov.vkfilter.NotificationService.GCMStation
import org.json.JSONObject
import java.util.Collections
import java.util.Vector
import kotlin.properties.Delegates


object ResponseHandler {
    public fun handle(request: VkRequestBundle, result: JSONObject) {
        when (request.vkFun) {
            VkFun.dialogList -> dialogList(request, result)
            VkFun.friendList -> friendList(request, result)
            VkFun.messageList -> messageList(request, result)
            VkFun.userInfo -> userInfo(request, result)
            VkFun.chatInfo -> chatInfo(request, result)
            VkFun.markIncomesAsRead -> markIncomesAsRead(request, result)
            VkFun.refreshDialog -> refreshDialog(request, result)
            VkFun.sendMessage -> sendMessage(request, result)
            VkFun.notificationInfo -> notificationInfo(request, result)
        }
    }

    private fun dialogList(request: VkRequestBundle, result: JSONObject) {
        object : AsyncTask<Unit, Unit, Unit>() {
            var newSnap: DialogListSnapshot by Delegates.notNull()
            val users = Vector<User>()
            override fun doInBackground(vararg params: Unit?) {
                val offset = request.vkParams["offset"] as Int
                val jsonUsers = JSONParser detailedDialogsResponseToUserList result
                val jsonDialogs = JSONParser detailedDialogsResponseToDialogList result

                users addAll (JSONParser parseUsers jsonUsers)
                for (u in users)
                    UserCache.putUser(u.id, u)

                val dialogs = JSONParser parseDialogs jsonDialogs
                val prevSnap = DialogListCache.getSnapshot()
                val mergedList = Vector<Dialog>()
                mergedList addAll prevSnap.dialogs.subList(0, offset)
                mergedList addAll dialogs
                newSnap = DialogListSnapshot(System.currentTimeMillis(), mergedList)
            }
            override fun onPostExecute(result: Unit?) {
                DialogListCache updateSnapshot newSnap
                UserCache.dataUpdated()
            }
        }.execute()
    }

    private fun friendList(request: VkRequestBundle, result: JSONObject) {
        val offset = request.vkParams["offset"] as Int
        val jsonUsers = JSONParser friendListResponseToUserList result

        val friends = JSONParser parseUsers jsonUsers
        for (u in friends)
            UserCache.putUser(u.id, u)
        if (offset == 0)
            FriendsListCache reloadList friends
        else
            FriendsListCache addItems friends
    }

    private fun messageList(request: VkRequestBundle, result: JSONObject) {
        val p = request.vkParams
        object : AsyncTask<Unit, Unit, Unit>() {
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
            var messages: Vector<Message> by Delegates.notNull()

            override fun doInBackground(vararg params: Unit?) {
                val jsonMessages = JSONParser messageListResponseToMessageList result
                messages = JSONParser parseMessages jsonMessages
                if (firstMessageIsUseless && messages.isNotEmpty())
                    messages.remove(0)
            }

            override fun onPostExecute(result: Unit) {
                val originalCount = count - (if (firstMessageIsUseless) 1 else 0)
                MessageCache
                        .getDialog(id, isChat)
                        .addMessagesWithReplace(messages, messages.size() < originalCount)
            }
        }.execute()
    }

    private fun userInfo(request: VkRequestBundle, result: JSONObject) {
        val jsonUsers = JSONParser userInfoResponseToUserList result
        val users = JSONParser parseUsers jsonUsers
        for (u in users)
            UserCache.putUser(u.id, u)
        UserCache.dataUpdated()
    }

    private fun chatInfo(request: VkRequestBundle, result: JSONObject) {
        val jsonUsers = JSONParser chatInfoResponseToUserList result
        val users = JSONParser parseUsers jsonUsers
        users forEach { UserCache.putUser(it.id, it) }
        UserCache.dataUpdated()

        val jsonChats = JSONParser chatInfoResponseToChatList result
        val chats = JSONParser parseChatInfo jsonChats
        chats forEach { ChatInfoCache.putChat(it.id.toString(), it) }
        ChatInfoCache.dataUpdated()
    }

    private fun markIncomesAsRead(request: VkRequestBundle, result: JSONObject) {
        val response = result.getInt("response")
        val id = request.additionalParams.getString("id")
        val chat = request.additionalParams.getBoolean("chat")
        if (response == 1) {
            MessageCache.getDialog(id, chat).markIncomesAsRead()
        } else {
            Handler().postDelayed({
                RunFun.markIncomesAsRead(id, chat)
            }, 1000)
        }
    }

    private fun refreshDialog(request: VkRequestBundle, result: JSONObject) {
        if (result.isNull("response"))
            DialogRefresher.onDataUpdate()
        else {
            val p = request.vkParams

            val chat = p contains "chat_id"
            val id = if (!chat)
                         p["user_id"] as String
                     else
                         p["chat_id"] as String

            val response = result.getJSONObject("response")
            if (response.has("read")) {
                val lastReadId = response.getLong("read")
                MessageCache.getDialog(id, chat).markOutcomesAsRead(lastReadId)
            }
            if (response.has("new_messages")) {
                val jsonMessages = response.getJSONArray("new_messages")
                val messages = JSONParser parseMessages jsonMessages
                MessageCache.getDialog(id, chat).addMessagesWithReplace(messages, false)
            }
        }
    }

    private fun sendMessage(request: VkRequestBundle, result: JSONObject) {
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
        MessageCache
                .getDialog(id, chat)
                .addMessagesWithReplace(
                    msgs = Collections.singleton(sentMessage),
                    itsAll = false
                )
    }

    private fun notificationInfo(request: VkRequestBundle, result: JSONObject) {
        val info = JSONParser parseNotificationInfo (JSONParser notificationInfoToObject result)
        GCMStation onLoadNotification info
    }
}