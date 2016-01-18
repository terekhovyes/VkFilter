package me.alexeyterekhov.vkfilter.Data.Entities.Message

class LinkAttachment(
        val title: String,
        val url: String
) : Cloneable {
    override public fun clone() = LinkAttachment(title, url)
}