package me.alexeyterekhov.vkfilter.Data.Entities.Message

class AudioAttachment(
        val artist: String,
        val title: String,
        val durationInSec: Int,
        val url: String
) : Cloneable {
    override public fun clone() = AudioAttachment(
            artist,
            title,
            durationInSec,
            url)
}