package me.alexeyterekhov.vkfilter.Data.Entities.Message

class StickerAttachment(
        smallSizeUrl: String,
        fullSizeUrl: String,
        width: Int,
        height: Int
) : ImageAttachment(
        smallSizeUrl,
        fullSizeUrl,
        width,
        height
)