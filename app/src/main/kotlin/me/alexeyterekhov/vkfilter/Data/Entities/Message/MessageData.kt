package me.alexeyterekhov.vkfilter.Data.Entities.Message

import java.util.*

class MessageData : Cloneable {
    var text = ""
    var images: List<ImageAttachment> = LinkedList()
    var stickers: List<StickerAttachment> = LinkedList()
    var documents: List<DocAttachment> = LinkedList()
    var audios: List<AudioAttachment> = LinkedList()
    var videos: List<VideoAttachment> = LinkedList()
    var links: List<LinkAttachment> = LinkedList()
    var walls: List<WallAttachment> = LinkedList()
    var messages: List<Message> = LinkedList()

    override public fun clone(): Any {
        val copy = MessageData()

        copy.text = text
        copy.images = copyList(images)
        copy.stickers = copyList(stickers)
        copy.documents = copyList(documents)
        copy.audios = copyList(audios)
        copy.videos = copyList(videos)
        copy.links = copyList(links)
        copy.walls = copyList(walls)
        copy.messages = copyList(messages)

        return copy
    }

    private fun <Type>copyList(list: List<Cloneable>) = Vector(list.map { it.clone() as Type })
}