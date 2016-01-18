package me.alexeyterekhov.vkfilter.Data.Entities.Message

class VideoAttachment(
        val id: Long,
        val title: String,
        val durationSec: Int,
        val previewUrl: String,
        var playerUrl: String
) : Cloneable {
    var requestKey = ""

    override public fun clone(): Any {
        val copy = VideoAttachment(
                id,
                title,
                durationSec,
                previewUrl,
                playerUrl)
        copy.requestKey = requestKey
        return copy
    }
}