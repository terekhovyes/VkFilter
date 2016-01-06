package me.alexeyterekhov.vkfilter.DataClasses.Attachments

class LinkAttachment(
        val title: String,
        val url: String
) {
    fun copy() = LinkAttachment(title, url)
}