package me.alexeyterekhov.vkfilter.DataClasses.Attachments

class ImageAttachment(
        val smallSizeUrl: String,
        val fullSizeUrl: String,
        val width: Int,
        val height: Int
) {
    fun copy() = ImageAttachment(
            smallSizeUrl,
            fullSizeUrl,
            width,
            height)
}