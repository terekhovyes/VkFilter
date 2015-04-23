package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.R


class ForwardMessageViewGenerator {
    fun inflateFor(
            message: Message,
            attachmentGenerator: AttachmentsViewGenerator,
            inflater: LayoutInflater,
            root: ViewGroup
    ): List<View> {
        return message.forwardMessages map {
            val holder = messageToView(attachmentGenerator, it, inflater, root)
            inflateFor(it, attachmentGenerator, inflater, holder.forwardMessagesLayout) forEach {
                holder addAttachment it
            }
            holder.view
        }
    }

    private fun messageToView(a: AttachmentsViewGenerator, m: Message, i: LayoutInflater, root: ViewGroup): ForwardMessageHolder {
        val view = i.inflate(R.layout.item_fwd_message, root, false)
        val holder = ForwardMessageHolder(view)
        with (holder) {
            if (UserCache.contains(m.senderId))
                fillUserInfo(UserCache.getUser(m.senderId)!!)
            else
                fillUserNotLoaded()
            setDate(m.dateMSC)
            setMessageText(m.text)
            a.inflate(m.attachments, i, attachmentsLayout) forEach {
                addAttachment(it)
            }
        }
        return holder
    }
}