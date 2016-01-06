package me.alexeyterekhov.vkfilter.DataClasses.Attachments

class DocAttachment(
        val title: String,
        val sizeInBytes: Int,
        val url: String
) {
    fun copy() = DocAttachment(
            title,
            sizeInBytes,
            url)
}