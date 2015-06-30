package me.alexeyterekhov.vkfilter.Internet

import android.util.Log
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.*
import me.alexeyterekhov.vkfilter.DataClasses.ChatInfo
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


public object JSONParser {
    private fun JSONArray.asListOfJSON(): List<JSONObject> {
        val list = LinkedList<JSONObject>()
        for (i in 0..this.length() - 1)
            list add this.getJSONObject(i)
        return list
    }

    fun parseUsers(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemUser(it) })
    fun parseDialogs(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemDialog(it.getJSONObject("message")) })
    fun parseMessages(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemMessage(it) })
    fun parseVideoUrls(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemVideoUrl(it) })
    fun parseChats(array: JSONArray) = Vector(array.asListOfJSON() map { parseItemChat(it) })
    fun parseNotification(response: JSONObject): NotificationInfo {
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
                    is Int -> lastOnlineTime = l.toLong()
                    is Long -> lastOnlineTime = l
                    else -> lastOnlineTime = l as JSONObject getLong "time"
                }
            }
        }
        return user
    }

    private fun parseItemDialog(item: JSONObject): Dialog {
        val dialog = Dialog()
        dialog.lastMessage = parseItemMessage(item)
        dialog.id = item.optLong("chat_id", item.optLong("user_id", 0))
        dialog.photoUrl = findPhotoMax(item) ?: ""
        val title = item.getString("title")
        if (title != " ... ")
            dialog.title = title

        // fill conversation partners
        if (item.has("chat_id")) {
            val partners = item.getJSONArray("chat_active")
            for (j in 0..partners.length() - 1)
                dialog.addPartner(UserCache.getUser(partners.getString(j))!!)
        } else
            dialog.addPartner(UserCache.getUser(item.getString("user_id"))!!)

        return dialog
    }

    private fun parseItemMessage(item: JSONObject): Message {
        val userId = item.optString("user_id", "")
        val out = item.optInt("out", -1) == 1

        val message = Message(if (out) UserCache.getMyId() else userId)
        with (message) {
            sentId = item.optLong("id", 0L)
            sentState = Message.STATE_SENT
            isOut = out
            text = item.optString("body", "")
            sentTimeMillis = item.optLong("date", 0L) * 1000L
            isRead = item.optInt("read_state", 1) == 1
        }

        if (item.has("fwd_messages"))
            message.attachments.messages addAll (parseMessages(item.getJSONArray("fwd_messages")))

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
                    "sticker" -> {
                        val image = parseStickerAttachment(it.getJSONObject("sticker"))
                        if (image.fullSizeUrl != "")
                            attachments.images add image
                    }
                    "doc" -> {
                        val doc = parseDocAttachment(it.getJSONObject("doc"))
                        if (doc.url != "")
                            attachments.documents add doc
                    }
                    "audio" -> {
                        val audio = parseAudioAttachment(it.getJSONObject("audio"))
                        if (audio.url != "")
                            attachments.audios add audio
                    }
                    "video" -> {
                        val video = parseVideoAttachment(it.getJSONObject("video"))
                        attachments.videos add video
                        if (video.playerUrl == "")
                            video.requestKey = videoJsonToRequestId(it.getJSONObject("video"))
                    }
                    "link" -> {
                        val link = parseLinkAttachment(it.getJSONObject("link"))
                        if (link.url != "")
                            attachments.links add link
                    }
                    "wall" -> {
                        val wall = parseWallAttachment(it.getJSONObject("wall"))
                        attachments.walls add wall
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("JSONParser", "Error parsing attachment")
            }
        }
    }

    fun parseImageAttachment(json: JSONObject): ImageAttachment {
        val smallSize = 700
        val width = json.optInt("width", 1)
        val height = json.optInt("height", 1)
        val url = findPhotoMax(json) ?: ""
        val smallUrl = findPhotoLess(smallSize, json) ?: ""
        return ImageAttachment(smallUrl, url, width, height)
    }
    private fun parseStickerAttachment(json: JSONObject): ImageAttachment {
        val width = json.optInt("width", 1)
        val height = json.optInt("height", 1)
        val url = findPhotoMax(json) ?: ""
        return ImageAttachment(url, url, width, height)
    }
    private fun parseDocAttachment(json: JSONObject): DocAttachment {
        val title = json.optString("title", "???")
        val size = json.optInt("size", -1)
        val url = json.optString("url")
        return DocAttachment(title, size, url)
    }
    private fun parseAudioAttachment(json: JSONObject): AudioAttachment {
        val artist = json.optString("artist")
        val title = json.optString("title")
        val duration = json.optInt("duration", -1)
        val url = json.optString("url")
        return AudioAttachment(artist, title, duration, url)
    }
    private fun parseVideoAttachment(json: JSONObject): VideoAttachment {
        val id = json.getLong("id")
        val title = json.optString("title")
        val duration = json.optInt("duration", -1)
        val previewImageUrl = findPhotoMax(json) ?: ""
        val playerUrl = json.optString("player")
        return VideoAttachment(
                id,
                title,
                duration,
                previewImageUrl,
                playerUrl
        )
    }

    private fun parseLinkAttachment(json: JSONObject): LinkAttachment {
        val url = json.optString("url", "")
        val title = json.optString("title", "")
        return LinkAttachment(title, url)
    }

    private fun parseWallAttachment(json: JSONObject): WallAttachment {
        return WallAttachment()
    }

    private fun parseItemChat(item: JSONObject): ChatInfo {
        val info = ChatInfo()
        info.id = item getLong "id"
        val users = item getJSONArray "users"
        for (i in 0..users.length() - 1) {
            val id = users.getLong(i).toString()
            if (UserCache contains id)
                info.chatPartners add (UserCache getUser id)
        }
        info.title = item.optString("title")
        info.photoUrl = findPhotoMax(item) ?: ""
        return info
    }

    private fun parseItemVideoUrl(json: JSONObject): VideoAttachment {
        val id = json.getLong("id")
        val url = json.getString("player")
        return VideoAttachment(
                id = id,
                title = "",
                durationSec = 0,
                previewUrl = "",
                playerUrl = url
        )
    }

    // Util methods

    private fun findPhotoMax(json: JSONObject): String? {
        if (json.has("photo_max"))
            return json getString "photo_max"
        val sizes = findAllPhotoSizes(json)
        if (sizes.isEmpty())
            return null
        return json getString "photo_${sizes.last()}"
    }
    private fun findPhotoLess(value: Int, json: JSONObject): String? {
        val sizes = findAllPhotoSizes(json)
        return when {
            sizes.isEmpty() && json.has("photo_max") -> json getString "photo_max"
            sizes.isEmpty() -> null
            sizes.first() > value -> json getString "photo_${sizes.first()}"
            else -> json getString "photo_${sizes.filter { it <= value }.last()}"
        }
    }
    private fun findAllPhotoSizes(json: JSONObject): List<Int> {
        return json.keys().asSequence().toArrayList()
                .filter { it.startsWith("photo_") }
                .map { it.substring(6).toInt() }
                .filter { json.has("photo_$it") }
                .sort()
    }

    private fun videoJsonToRequestId(json: JSONObject): String {
        val id = json.getString("id")
        val owner = json.optString("owner_id")
        val accessKey = json.optString("access_key")

        val begin = if (owner == "")
            id
        else
            "${owner}_${id}"

        return if (accessKey == "")
            begin
        else
            "${begin}_${accessKey}"
    }
}