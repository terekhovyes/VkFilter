package me.alexeyterekhov.vkfilter.Internet

import android.util.Log
import me.alexeyterekhov.vkfilter.Data.Cache.UserCache
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.Dialog
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.*
import me.alexeyterekhov.vkfilter.Data.Entities.User.Device
import me.alexeyterekhov.vkfilter.Data.Entities.User.Sex
import me.alexeyterekhov.vkfilter.Data.Entities.User.User
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object JsonParserNew {
    private fun JSONArray.asListOfJSON(): List<JSONObject> {
        val list = LinkedList<JSONObject>()
        for (i in 0..this.length() - 1)
            list.add(this.getJSONObject(i))
        return list
    }

    private fun JSONArray.asListOfJSONArrays(): List<JSONArray> {
        val list = LinkedList<JSONArray>()
        for (i in 0..this.length() - 1)
            list.add(this.getJSONArray(i))
        return list
    }

    fun parseUsers(array: JSONArray) = Vector(array.asListOfJSON().map { parseItemUser(it) })

    fun parseChats(array: JSONArray) = Vector(array.asListOfJSON().map { parseItemChat(it) })

    fun parseDialogs(array: JSONArray) = Vector(array.asListOfJSON().map { parseItemDialog(it.getJSONObject("message")) })

    fun parseMessages(array: JSONArray) = Vector(array.asListOfJSON().map { parseItemMessage(it) })

    fun parseVideoUrls(array: JSONArray) = Vector(array.asListOfJSON().map { parseItemVideoUrl(it) })

    fun parseItemUser(item: JSONObject): User {
        val user = User()

        user.id = item.getString("id")
        user.firstName = item.getString("first_name")
        user.lastName = item.getString("last_name")
        user.photoUrl = item.optString("photo_max", "")
        user.sex = when (item.optInt("sex", 0)) {
            1 -> Sex.WOMAN
            2 -> Sex.MAN
            else -> Sex.UNKNOWN
        }
        user.onlineStatus.isOnline = item.optInt("online", 0) == 1
        user.onlineStatus.lastVisitTime = when {
            !item.has("last_seen") || item.isNull("last_seen") -> 0L
            item.get("last_seen") is Int -> item.getInt("last_seen").toLong()
            item.get("last_seen") is Long -> item.getLong("last_seen")
            else -> item.getJSONObject("last_seen").optLong("time", 0L)
        }
        user.onlineStatus.device = when {
            item.has("last_seen")
                    && !item.isNull("last_seen")
                    && item.get("last_seen") is JSONObject
            -> parseDeviceType(item.getJSONObject("last_seen"))
            else -> Device.DESKTOP
        }

        return user
    }

    fun parseItemChat(item: JSONObject): Dialog {
        val jsonUserIdList = item.getJSONArray("users")
        val chat = Dialog()

        chat.id = DialogId(item.getLong("id"), true)
        chat.partners = (0..jsonUserIdList.length() - 1)
                .map { jsonUserIdList.getLong(it).toString() }
                .filter { UserCache.contains(it) }
                .map { UserCache.getUser(it)!! }
        chat.specialTitle = item.optString("title")
        chat.specialPhotoUrl = findPhotoMax(item) ?: ""

        return chat
    }

    fun parseItemDialog(item: JSONObject): Dialog {
        val isChat = item.has("chat_id")
        val id = item.optLong(if (isChat) "chat_id" else "user_id")
        val title = item.getString("title")
        val dialog = Dialog()

        dialog.id = DialogId(id, isChat)
        dialog.specialPhotoUrl = findPhotoMax(item) ?: ""
        if (title != " ... ")
            dialog.specialTitle = title
        dialog.partners = if (isChat) {
            val partners = item.getJSONArray("chat_active")
            (0..partners.length() - 1)
                    .map { partners.getString(it) }
                    .map { UserCache.getUser(it)!! }
        } else
            Collections.singletonList(UserCache.getUser(item.getString("user_id"))!!)
        dialog.messages.history = Collections.singletonList(parseItemMessage(item))

        return dialog
    }

    fun parseItemMessage(item: JSONObject): Message {
        val userId = item.optString("user_id", "")
        val out = item.optInt("out", -1) == 1
        val message = Message(if (out) UserCache.getMyId() else userId)

        message.isOut = out
        message.isRead = item.optInt("read_state", 1) == 1
        message.sent.id = item.optLong("id", 0L)
        message.sent.state = SentState.State.STATE_SENT
        message.sent.timeMillis = item.optLong("date", 0L) * 1000L
        message.data.text = item.optString("body", "")
        if (item.has("fwd_messages"))
            message.data.messages = parseMessages(item.getJSONArray("fwd_messages"))
        if (item.has("attachments"))
            parseMessageAttachments(item.getJSONArray("attachments"), message.data)

        return message
    }

    fun parseDeviceType(lastSeen: JSONObject): Device {
        return when (lastSeen.optInt("platform", 7)) {
            in 1..5 -> Device.MOBILE
            in 6..7 -> Device.DESKTOP
            else -> Device.DESKTOP
        }
    }

    fun parseMessageAttachments(array: JSONArray, messageData: MessageData) {
        val images = Vector<ImageAttachment>()
        val stickers = Vector<StickerAttachment>()
        val docs = Vector<DocAttachment>()
        val audios = Vector<AudioAttachment>()
        val videos = Vector<VideoAttachment>()
        val links = Vector<LinkAttachment>()
        val walls = Vector<WallAttachment>()

        array.asListOfJSON().forEach {
            try {
                when (it.getString("type")) {
                    "photo" -> {
                        val image = parseImageAttachment(it.getJSONObject("photo"))
                        if (image.fullSizeUrl != "")
                            images.add(image)
                    }
                    "sticker" -> {
                        val sticker = parseStickerAttachment(it.getJSONObject("sticker"))
                        if (sticker.fullSizeUrl != "")
                            stickers.add(sticker)
                    }
                    "doc" -> {
                        val doc = parseDocAttachment(it.getJSONObject("doc"))
                        if (doc.url != "")
                            docs.add(doc)
                    }
                    "audio" -> {
                        val audio = parseAudioAttachment(it.getJSONObject("audio"))
                        if (audio.url != "")
                            audios.add(audio)
                    }
                    "video" -> {
                        val video = parseVideoAttachment(it.getJSONObject("video"))
                        videos.add(video)
                        if (video.playerUrl == "")
                            video.requestKey = videoJsonToRequestId(it.getJSONObject("video"))
                    }
                    "link" -> {
                        val link = parseLinkAttachment(it.getJSONObject("link"))
                        if (link.url != "")
                            links.add(link)
                    }
                    "wall" -> {
                        val wall = parseWallAttachment(it.getJSONObject("wall"))
                        walls.add(wall)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("JSONParser", "Error parsing attachment")
            }
        }

        messageData.images = images
        messageData.stickers = stickers
        messageData.documents = docs
        messageData.audios = audios
        messageData.videos = videos
        messageData.links = links
        messageData.walls = walls
    }

    fun parseImageAttachment(json: JSONObject): ImageAttachment {
        val smallSize = 700
        val width = json.optInt("width", 1)
        val height = json.optInt("height", 1)
        val url = findPhotoMax(json) ?: ""
        val smallUrl = findPhotoLess(smallSize, json) ?: ""
        return ImageAttachment(smallUrl, url, width, height)
    }

    fun parseStickerAttachment(json: JSONObject): StickerAttachment {
        val width = json.optInt("width", 1)
        val height = json.optInt("height", 1)
        val url = findPhotoMax(json) ?: ""
        return StickerAttachment(url, url, width, height)
    }

    fun parseDocAttachment(json: JSONObject): DocAttachment {
        val title = json.optString("title", "???")
        val size = json.optInt("size", -1)
        val url = json.optString("url")
        return DocAttachment(title, size, url)
    }

    fun parseAudioAttachment(json: JSONObject): AudioAttachment {
        val artist = json.optString("artist")
        val title = json.optString("title")
        val duration = json.optInt("duration", -1)
        val url = json.optString("url")
        return AudioAttachment(artist, title, duration, url)
    }

    fun parseVideoAttachment(json: JSONObject): VideoAttachment {
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

    fun parseLinkAttachment(json: JSONObject): LinkAttachment {
        val url = json.optString("url", "")
        val title = json.optString("title", "")
        return LinkAttachment(title, url)
    }

    fun parseWallAttachment(json: JSONObject): WallAttachment {
        return WallAttachment()
    }

    fun parseItemVideoUrl(json: JSONObject): VideoAttachment {
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

    // Utils

    private fun findPhotoMax(json: JSONObject): String? {
        if (json.has("photo_max"))
            return json.getString("photo_max")
        val sizes = findAllPhotoSizes(json)
        if (sizes.isEmpty())
            return null
        return json.getString("photo_${sizes.last()}")
    }

    private fun findPhotoLess(value: Int, json: JSONObject): String? {
        val sizes = findAllPhotoSizes(json)
        return when {
            sizes.isEmpty() && json.has("photo_max") -> json.getString("photo_max")
            sizes.isEmpty() -> null
            sizes.first() > value -> json.getString("photo_${sizes.first()}")
            else -> json.getString("photo_${sizes.filter { it <= value }.last()}")
        }
    }

    private fun findAllPhotoSizes(json: JSONObject): List<Int> {
        return json.keys().asSequence().toArrayList()
                .filter { it.startsWith("photo_") }
                .map { it.substring(6).toInt() }
                .filter { json.has("photo_$it") }
                .sorted()
    }

    private fun videoJsonToRequestId(json: JSONObject): String {
        val id = json.getString("id")
        val owner = json.optString("owner_id")
        val accessKey = json.optString("access_key")

        val begin = if (owner == "")
            id
        else
            "${owner}_$id"

        return if (accessKey == "")
            begin
        else
            "${begin}_$accessKey"
    }
}