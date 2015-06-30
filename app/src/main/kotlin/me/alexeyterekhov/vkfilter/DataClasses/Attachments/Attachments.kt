package me.alexeyterekhov.vkfilter.DataClasses.Attachments

import me.alexeyterekhov.vkfilter.DataClasses.Message
import java.util.Vector

class Attachments {
    val images = Vector<ImageAttachment>()
    val documents = Vector<DocAttachment>()
    val audios = Vector<AudioAttachment>()
    val videos = Vector<VideoAttachment>()
    val links = Vector<LinkAttachment>()
    val walls = Vector<WallAttachment>()
    val messages = Vector<Message>()
}