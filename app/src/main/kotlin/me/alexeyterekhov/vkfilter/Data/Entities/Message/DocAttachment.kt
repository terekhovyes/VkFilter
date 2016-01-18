package me.alexeyterekhov.vkfilter.Data.Entities.Message

class DocAttachment(
        val title: String,
        val sizeInBytes: Int,
        val url: String
) : Cloneable {
    override public fun clone() = DocAttachment(
            title,
            sizeInBytes,
            url)
}