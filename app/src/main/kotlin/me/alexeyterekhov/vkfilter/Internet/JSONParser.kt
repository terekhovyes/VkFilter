package me.alexeyterekhov.vkfilter.Internet

import android.util.Log
import me.alexeyterekhov.vkfilter.Common.DateFormat
import me.alexeyterekhov.vkfilter.DataCache.Helpers.ChatInfo
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.ImageAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.DataClasses.Sex
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.NotificationService.NotificationInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.LinkedList
import java.util.Vector

fun JSONArray.asListOfJSON(): List<JSONObject> {
    val list = LinkedList<JSONObject>()
    for (i in 0..this.length() - 1)
        list add this.getJSONObject(i)
    return list
}

object JSONParser {
    // Util

    private fun emptyUser() = User()

    // Converters

    fun detailedDialogsResponseToDialogList(response: JSONObject): JSONArray {
        val respObj = response.getJSONObject("response")
        return respObj.getJSONArray("items")
    }

    fun detailedDialogsResponseToUserList(response: JSONObject): JSONArray {
        val respObj = response.getJSONObject("response")
        return respObj.getJSONArray("user_info")
    }

    fun friendListResponseToUserList(response: JSONObject): JSONArray {
        val respObj = response.getJSONObject("response")
        return respObj.getJSONArray("items")
    }

    fun messageListResponseToMessageList(response: JSONObject): JSONArray {
        val respObj = response.getJSONObject("response")
        return respObj.getJSONArray("items")
    }

    fun userInfoResponseToUserList(response: JSONObject): JSONArray {
        return response.getJSONArray("response")
    }

    fun chatInfoResponseToUserList(response: JSONObject): JSONArray {
        val respObj = response.getJSONObject("response")
        return respObj.getJSONArray("user_info")
    }

    fun chatInfoResponseToChatList(response: JSONObject): JSONArray {
        val respObj = response.getJSONObject("response")
        return respObj.getJSONArray("chats")
    }

    fun notificationInfoToObject(response: JSONObject): JSONObject {
        return response getJSONObject "response"
    }

    fun dialogPartnersToArray(response: JSONObject): JSONArray {
        return response getJSONArray "response"
    }

    // Parsers

    fun parseDialogs(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemDialog(it.getJSONObject("message")) })
    fun parseUsers(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemUser(it) })
    fun parseMessages(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemMessage(it) })
    fun parseChatInfo(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemChatInfo(it) })

    fun parseNotificationInfo(response: JSONObject): NotificationInfo {
        var info = NotificationInfo()
        with (info) {
            messageId = response getString "message_id"
            date = response getLong "date"
            text = response optString "text"
            senderId = response optString "user_id"
            firstName = response optString "first_name"
            lastName = response optString "last_name"
            senderPhotoUrl = response optString "user_photo"
            chatId = response optString "chat_id"
            chatTitle = response optString "title"
            chatPhotoUrl = response optString "chat_photo"
        }
        return info
    }

    private fun parseItemChatInfo(item: JSONObject): ChatInfo {
        val info = ChatInfo()
        info.id = item getLong "id"
        val users = item getJSONArray "users"
        for (i in 0..users.length() - 1) {
            val id = users.getLong(i).toString()
            if (UserCache contains id)
                info.chatPartners add (UserCache getUser id)
        }
        if (item has "title")
            info.title = item getString "title"
        arrayListOf("photo_50", "photo_100", "photo_200") forEach {
            if (item has it)
                info.photoUrl = item getString it
        }
        return info
    }

    private fun parseItemDialog(item: JSONObject): Dialog {
        val dialog = Dialog()

        val id = if (item.getInt("out") == 1) "me" else item.getString("user_id")

        // fill last message
        val dateInSeconds = item.getLong("date")
        val message = Message(id)
        with (message) {
            dateMSC = dateInSeconds * 1000L
            formattedDate = DateFormat.dialogReceivedDate(dateInSeconds)
            text = item.optString("body", "")
            isRead = item.optInt("read_state", 1) == 1
            isOut = item.optInt("out", 0) == 1
        }
        dialog.lastMessage = message

        dialog.id = item.optLong("chat_id", item.optLong("user_id", 0))

        // fill conversation partners
        if (item.has("chat_id")) {
            val partners = item.getJSONArray("chat_active")
            for (j in 0..partners.length() - 1)
                dialog.addPartner(UserCache.getUser(partners.getString(j))!!)
        } else
            dialog.addPartner(UserCache.getUser(item.getString("user_id"))!!)

        // check if dialog has own picture and title
        val title = item.getString("title")
        if (title != " ... ")
            dialog.title = title

        val photoSizes = arrayListOf("photo_200", "photo_100", "photo_50")
        val availableSize = photoSizes firstOrNull { item has it }
        dialog.photoUrl = if (availableSize != null)
            item.getString(availableSize)
        else
            ""

        return dialog
    }

    private fun parseItemUser(item: JSONObject): User {
        val user = User()
        with (user) {
            id = item.getString("id")
            firstName = item.getString("first_name")
            lastName = item.getString("last_name")
            photoUrl = if (item.isNull("photo_max")) "" else item.getString("photo_max")
            sex = when {
                item.isNull("sex"), item.getInt("sex") == 0 -> Sex.UNKNOWN
                item.getInt("sex") == 1 -> Sex.WOMAN
                item.getInt("sex") == 2 -> Sex.MAN
                else -> Sex.UNKNOWN
            }
            isOnline = item has "online" && (item getInt "online") == 1
            onlineStatusChanged = (UserCache.contains(id)
                    && UserCache.getUser(id)!!.isOnline != isOnline)
            if (item.has("last_seen") && !item.isNull("last_seen")) {
                val l = item get "last_seen"
                when (l) {
                    is Int -> lastOnlineTime = (l as Int).toLong()
                    is Long -> lastOnlineTime = l as Long
                    else -> lastOnlineTime = l as JSONObject getLong "time"
                }
            }
        }
        return user
    }

    private fun parseItemMessage(item: JSONObject): Message {
        val userId = item.optString("user_id", "")
        val out = item.optInt("out", -1) == 1

        val message = Message(if (out) "me" else userId)
        with (message) {
            id = item.optLong("id", 0L)
            isOut = out
            text = item.optString("body", "")
            dateMSC = item.optLong("date", 0L) * 1000L
            formattedDate = if (dateMSC != 0L)
                DateFormat.time(dateMSC / 1000L)
            else
                ""
            isRead = item.optInt("read_state", 1) == 1
        }

        if (item.has("fwd_messages"))
            message.forwardMessages addAll (parseMessages(item.getJSONArray("fwd_messages")))

        if (item.has("attachments"))
            parseMessageAttachments(item.getJSONArray("attachments"), message.attachments)

        return message
    }

    private fun parseMessageAttachments(array: JSONArray, attachments: Attachments) {
        array.asListOfJSON() forEach {
            try {
                when (it.getString("type")) {
                    "photo" -> {
                        val image = parseImageAttachment(it.getJSONObject("photo"))
                        if (image.fullSizeUrl != "")
                            attachments.images add image
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("JSONParser", "Error parsing attachment")
            }
        }
    }

    private fun parseImageAttachment(json: JSONObject): ImageAttachment {
        val photoSizes = arrayListOf("photo_2560", "photo_1280", "photo_807",
                "photo_604", "photo_130", "photo_75")
        val bigSizeCount = 3

        val width = json.optInt("width", 1)
        val height = json.optInt("height", 1)
        val url = json getString (photoSizes first { json has it })
        val smallUrl = json getString (photoSizes drop bigSizeCount first { json has it })

        return ImageAttachment(smallUrl, url, width, height)
    }
}