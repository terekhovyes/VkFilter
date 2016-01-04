package me.alexeyterekhov.vkfilter.Internet

import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Message
import java.util.*

public object MissingDataAnalyzer {
    fun missingUsersIds(messages: Collection<Message>): List<String> {
        fun missingInMessage(m: Message): List<String> {
            val out = if (m.attachments.messages.isEmpty())
                LinkedList<String>()
            else
                m.attachments.messages
                    .map { missingInMessage(it) }
                    .foldRight(LinkedList<String>(), { el, list ->
                        list.addAll(el)
                        list
                    })
            if (!UserCache.contains(m.senderId))
                out.add(m.senderId)
            return out
        }

        val missingIds = messages
                .map { missingInMessage(it) }
                .foldRight(LinkedList<String>(), { el, list ->
                    list.addAll(el)
                    list
                })
        return missingIds
    }

    fun missingVideoIds(messages: Collection<Message>): List<String> {
        fun missingInMessage(m: Message): List<String> {
            val out = if (m.attachments.messages.isEmpty())
                LinkedList<String>()
            else
                m.attachments.messages
                    .map { missingInMessage(it) }
                    .foldRight(LinkedList<String>(), { el, list ->
                        list.addAll(el)
                        list
                    })

            out.addAll(m.attachments.videos
                    .filter { it.playerUrl == "" }
                    .map { it.requestKey })
            return out
        }

        val missingIds = messages
                .map { missingInMessage(it) }
                .foldRight(LinkedList<String>(), { el, list ->
                    list.addAll(el)
                    list
                })

        return missingIds
    }
}