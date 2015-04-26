package me.alexeyterekhov.vkfilter.DataClasses.Attachments

import java.util.Vector

public class Attachments {
    val images = Vector<ImageAttachment>()
    val documents = Vector<DocAttachment>()
    val audios = Vector<AudioAttachment>()
    val videos = Vector<VideoAttachment>()
}