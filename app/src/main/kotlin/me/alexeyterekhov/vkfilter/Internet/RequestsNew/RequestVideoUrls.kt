package me.alexeyterekhov.vkfilter.Internet.RequestsNew

import me.alexeyterekhov.vkfilter.Data.Cache.DialogCache
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message
import me.alexeyterekhov.vkfilter.Data.Entities.Message.VideoAttachment
import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Internet.JsonParserNew
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import org.json.JSONObject
import java.util.*

class RequestVideoUrls(
        val dialogId: DialogId,
        val videoIds: Collection<String>
) : Request("execute.videoUrls") {
    init {
        params["video_ids"] = "${videoIds.joinToString(separator = ",")}"
    }

    override fun handleResponse(json: JSONObject) {
        if (!DialogCache.contains(dialogId))
            return

        val jsonVideoArray = json.getJSONArray("response")
        val videos = JsonParserNew.parseVideoUrls(jsonVideoArray)
        val idSet = (videos.map { it.id }).toSet()

        val messagesForUpdate = DialogCache
                .getDialog(dialogId)!!
                .messages
                .history
                .filter { containsVideo(idSet, it) }

        videos.forEach {
            val videoId = it.id
            val playerUrl = it.playerUrl

            messagesForUpdate
                    .map { findAttachmentsWithId(videoId, it) }
                    .map { it.forEach { it.playerUrl = playerUrl } }
        }

        UpdateHandler.messages.postMessagesUpdated(dialogId, messagesForUpdate)
    }

    private fun findAttachmentsWithId(vid: Long, m: Message): LinkedList<VideoAttachment> {
        val list = if (m.data.messages.isNotEmpty())
            m.data.messages
                    .map { findAttachmentsWithId(vid, it) }
                    .foldRight(LinkedList<VideoAttachment>(), {
                        list, el ->
                        list.addAll(el)
                        list
                    })
        else
            LinkedList<VideoAttachment>()
        (m.data.videos.filter { it.id == vid }).forEach {
            list.add(it)
        }
        return list
    }

    private fun containsVideo(videoIds: Set<Long>, message: Message): Boolean = when {
        message.data.videos.any { videoIds.contains(it.id) }
            -> true
        message.data.messages.isNotEmpty()
            -> message.data.messages.any { containsVideo(videoIds, it) }
        else
            -> false
    }
}