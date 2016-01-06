package me.alexeyterekhov.vkfilter.DataClasses.Attachments

class AudioAttachment(
        val artist: String,
        val title: String,
        val durationInSec: Int,
        val url: String
) {
    fun copy() = AudioAttachment(
            artist,
            title,
            durationInSec,
            url)
}