package me.alexeyterekhov.vkfilter.DataClasses.Attachments

class WallAttachment {
    var wallId = ""
    var toId = ""
    var text = ""

    fun url() = "http://www.vk.com/wall${toId}_${wallId}"

    fun copy(): WallAttachment {
        val copy = WallAttachment()
        copy.toId = toId
        copy.text = text
        return copy
    }
}