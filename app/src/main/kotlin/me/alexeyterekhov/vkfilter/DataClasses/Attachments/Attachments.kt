package me.alexeyterekhov.vkfilter.DataClasses.Attachments

import me.alexeyterekhov.vkfilter.DataClasses.Message
import java.util.*

class Attachments {
    val images = Vector<ImageAttachment>()
    val documents = Vector<DocAttachment>()
    val audios = Vector<AudioAttachment>()
    val videos = Vector<VideoAttachment>()
    val links = Vector<LinkAttachment>()
    val walls = Vector<WallAttachment>()
    val messages = Vector<Message>()

    fun copyFrom(other: Attachments) {
        images.addAll(other.images.map { it.copy() })
        documents.addAll(other.documents.map { it.copy() })
        audios.addAll(other.audios.map { it.copy() })
        videos.addAll(other.videos.map { it.copy() })
        links.addAll(other.links.map { it.copy() })
        walls.addAll(other.walls.map { it.copy() })
        messages.addAll(other.messages.map { it.copy() })
    }
}