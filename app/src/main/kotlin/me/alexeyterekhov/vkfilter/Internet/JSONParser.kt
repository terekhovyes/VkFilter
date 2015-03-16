package me.alexeyterekhov.vkfilter.Internet

import org.json.JSONObject
import java.util.Vector
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Common.DateFormat
import org.json.JSONArray
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.DataClasses.Sex
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.ImageAttachment


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

    // Parsers

    fun parseDialogs(array: JSONArray): Vector<Dialog> {
        val dialogs = Vector<Dialog>()
        for (i in 0..array.length() - 1) {
            val item = array.getJSONObject(i).getJSONObject("message")
            val dialog = parseItemDialog(item)
            dialogs add dialog
        }
        return dialogs
    }

    fun parseUsers(array: JSONArray): Vector<User> {
        val users = Vector<User>()
        for (i in 0..array.length() - 1) {
            val json = array.getJSONObject(i)
            val user = parseItemUser(json)
            users add user
        }
        return users
    }

    fun parseMessages(array: JSONArray): Vector<Message> {
        val messages = Vector<Message>()
        for (i in 0..array.length() - 1) {
            val json = array.getJSONObject(i)
            val message = parseItemMessage(json)
            messages add message
        }
        return messages
    }

    private fun parseItemDialog(item: JSONObject): Dialog {
        val dialog = Dialog()

        val id = if (item.getInt("out") == 1) "me" else item.getString("user_id")
        var sender = UserCache.getUser(id)
        if (sender == null)
            sender = emptyUser()

        // fill last message
        val dateInSeconds = item.getLong("date")
        val message = Message(sender!!)
        with (message) {
            dateMSC = dateInSeconds * 1000L
            formattedDate = DateFormat.dialogReceivedDate(dateInSeconds)
            text = item.getString("body")
            isRead = item.getInt("read_state") == 1
            isOut = item.getInt("out") == 1
        }
        dialog.lastMessage = message

        dialog.id = if (item.has("chat_id")) item.getLong("chat_id") else item.getLong("user_id")

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
        var photoUrl = if (item.has("photo_200"))
            item.getString("photo_200")
        else if (item.has("photo_100"))
            item.getString("photo_100")
        else if (item.has("photo_50"))
            item.getString("photo_50")
        else ""
        dialog.photoUrl = photoUrl

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
        val userId = item.getString("user_id")
        val out = item.getInt("out") == 1
        val userExist = out && UserCache.contains("me") || !out && UserCache.contains(userId)

        val message = Message(
                if (!userExist)
                    emptyUser()
                else
                    if (out)
                        UserCache.getUser("me")!!
                    else
                        UserCache.getUser(userId)!!
        )
        with (message) {
            id = item.getLong("id")
            isOut = out
            text = item.getString("body")
            dateMSC = item.getLong("date") * 1000L
            formattedDate = DateFormat.time(dateMSC / 1000L)
            isRead = item.getInt("read_state") == 1
        }

        if (item.has("attachments"))
            parseMessageAttachments(item.getJSONArray("attachments"), message.attachments)

        return message
    }

    private fun parseMessageAttachments(array: JSONArray, attachments: Attachments) {
        for (i in 0..array.length() - 1) {
            val attachment = array.getJSONObject(i)
            if (attachment.getString("type") == "photo") {
                val photo = attachment.getJSONObject("photo")
                val width = photo.getInt("width")
                val height = photo.getInt("height")
                val photoSizes = arrayListOf("photo_2560", "photo_1280", "photo_807",
                        "photo_604", "photo_130", "photo_75")
                var url = ""
                var smallUrl = ""
                for (j in 0..photoSizes.size - 1) {
                    val size = photoSizes[j]
                    if (url == "" && photo.has(size))
                        url = photo.getString(size)
                    if (j > 1 && photo.has(size)) {
                        smallUrl = photo.getString(size)
                        break
                    }
                }
                attachments.images add ImageAttachment(smallUrl, url, width, height)
            }
        }
    }
}