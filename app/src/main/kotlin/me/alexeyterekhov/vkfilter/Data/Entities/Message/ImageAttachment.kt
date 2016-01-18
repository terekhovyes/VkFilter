package me.alexeyterekhov.vkfilter.Data.Entities.Message

open class ImageAttachment(
        val smallSizeUrl: String,
        val fullSizeUrl: String,
        val width: Int,
        val height: Int
) : Cloneable {
    override public fun clone() = ImageAttachment(
            smallSizeUrl,
            fullSizeUrl,
            width,
            height)
}