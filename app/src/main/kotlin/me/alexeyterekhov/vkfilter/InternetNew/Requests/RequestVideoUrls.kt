package me.alexeyterekhov.vkfilter.InternetNew.Requests

import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.VideoAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.InternetNew.JSONParser
import org.json.JSONObject
import java.util.LinkedList

class RequestVideoUrls(
        val dialogId: String,
        val isChat: Boolean,
        val videoIds: Collection<String>
) : Request("execute.videoUrls") {
    init {
        params["video_ids"] = "'${videoIds.joinToString(separator = ",")}'"
    }

    override fun handleResponse(json: JSONObject) {
        val jsonVideoList = json getJSONArray "response"
        val videoList = JSONParser parseVideoUrls jsonVideoList
        val idSet = (videoList map { it.id }).toSet()

        val cache = MessageCaches.getCache(dialogId, isChat)
        val messagesWithVideos = cache.getMessages() filter { containsVideo(idSet, it) }

        videoList forEach {
            val videoId = it.id
            val playerUrl = it.playerUrl

            messagesWithVideos
                    .map { findAttachmentsWithId(videoId, it) }
                    .map { it forEach { it.playerUrl = playerUrl } }
        }

        MessageCaches.getCache(dialogId, isChat).onUpdateMessages(messagesWithVideos)
    }

    private fun findAttachmentsWithId(vid: Long, m: Message): LinkedList<VideoAttachment> {
        val list = if (m.attachments.messages.isNotEmpty())
            m.attachments.messages
                    .map { findAttachmentsWithId(vid, it) }
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

    private fun containsVideo(videoIds: Set<Long>, message: Message): Boolean = when {
        message.attachments.videos any { videoIds contains it.id }
            -> true
        message.attachments.messages.isNotEmpty()
            -> message.attachments.messages any { containsVideo(videoIds, it) }
        else
            -> false
    }
}