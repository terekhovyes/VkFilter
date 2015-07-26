package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestReadMessages
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.DateFormat
import me.alexeyterekhov.vkfilter.Util.ImageLoadConf
import java.util.Calendar
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList
import kotlin.properties.Delegates

class ChatAdapter(
        val dialogId: String,
        val isChat: Boolean,
        val activity: AppCompatActivity
) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        MessageCacheListener
{
    val TYPE_IN = 1
    val TYPE_OUT = 2
    val TYPE_FOOTER = 3

    val READ_DURATION = 250L
    val READ_OFFSET = 1000L

    val TIME_FOR_ANIMATION = 400L
    var lastAnimationStartTime = 0L

    val selectedMessageIds = HashSet<Long>()
    var onSelectionChangeAction: (() -> Unit)? = null

    val inflater = LayoutInflater.from(activity)
    val messages = LinkedList<Message>()
    var attachmentGenerator: AttachmentsViewGenerator by Delegates.notNull()
    val shownImages = HashSet<String>()

    val readAnimationMessages = HashSet<Message>()
    val readAnimationStartTime = HashMap<Message, Long>()

    fun messagePosById(id: Long) = messages indexOfLast { it.sentId == id }
    fun messageById(id: Long) = messages last { it.sentId == id }
    fun selectMessage(id: Long) {
        selectedMessageIds add id
        val position = messagePosById(id)
        notifyItemChanged(position)
        notifyItemChanged(position + 1)
        onSelectionChangeAction?.invoke()
    }
    fun deselectMessage(id: Long) {
        selectedMessageIds remove id
        val position = messagePosById(id)
        notifyItemChanged(position)
        notifyItemChanged(position + 1)
        onSelectionChangeAction?.invoke()
    }
    fun deselectAllMessages() {
        val ids = HashSet(selectedMessageIds)
        selectedMessageIds.clear()
        notifyItemRangeChanged(0, getItemCount())
        onSelectionChangeAction?.invoke()
    }
    fun getSelectedMessageIds() = selectedMessageIds.toSortedList()

    override fun getItemCount() = if (messages.isNotEmpty()) messages.count() + 1 else 0
    override fun getItemViewType(pos: Int) = when {
        pos < messages.count() && messages[pos].isOut -> TYPE_OUT
        pos < messages.count() && messages[pos].isIn -> TYPE_IN
        else -> TYPE_FOOTER
    }
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val res = when (viewType) {
            TYPE_IN -> R.layout.message_in
            TYPE_OUT -> R.layout.message_out
            TYPE_FOOTER -> R.layout.message_footer
            else -> throw Exception("WRONG ITEM TYPE")
        }
        val view = inflater.inflate(res, parent, false)
        return when (viewType) {
            TYPE_IN -> HolderMessageIn(view)
            TYPE_OUT -> HolderMessageOut(view)
            TYPE_FOOTER -> HolderMessageFooter(view)
            else -> throw Exception("WRONG ITEM TYPE")
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            val footerHolder = holder as HolderMessageFooter
        } else {
            val baseHolder = holder as HolderMessageBase
            val message = messages[position]
            val messageFirstInChain = position == 0 || isFirstReply(position)
            val messageFirstInDay = position > 0 && !isSameDay(message.sentTimeMillis, messages[position - 1].sentTimeMillis)
            val messageFirstInDialog = position == 0 && MessageCaches.getCache(dialogId, isChat).historyLoaded
            val longClickListener = { view: View ->
                if (selectedMessageIds.isNotEmpty()) {
                    deselectAllMessages()
                } else {
                    selectMessage(message.sentId)
                    notifyItemRangeChanged(0, messages.count())
                }
                true
            }
            val shortClickListener = { view: View ->
                if (selectedMessageIds.isNotEmpty()) {
                    if (message.sentId in selectedMessageIds) {
                        if (selectedMessageIds.count() == 1)
                            deselectAllMessages()
                        else
                            deselectMessage(message.sentId)
                    } else
                        selectMessage(message.sentId)
                }
            }

            // Listeners
            val topSelIsClickable = selectedMessageIds.isNotEmpty() && message.sentState != Message.STATE_SENDING
            baseHolder.setTopSelectorClickable(topSelIsClickable)
            if (topSelIsClickable) {
                baseHolder.selectorTop setOnClickListener shortClickListener
                baseHolder.selectorTop setOnLongClickListener longClickListener
            }
            if (message.sentState != Message.STATE_SENDING)
                baseHolder.selectorBack setOnLongClickListener longClickListener

            // Colors
            when (getItemViewType(position)) {
                TYPE_IN -> {
                    val h = baseHolder as HolderMessageIn
                    h.setColors(selected = (selectedMessageIds contains message.sentId))
                }
                TYPE_OUT -> {
                    val h = baseHolder as HolderMessageOut
                    if (selectedMessageIds contains message.sentId)
                        h.setColorsSelected()
                    else
                        h.setColorsForState(message.sentState)
                }
            }

            // Specific attributes
            when (getItemViewType(position)) {
                TYPE_IN -> {
                    val h = baseHolder as HolderMessageIn
                    val showPhoto = isChat && (messageFirstInChain || messageFirstInDay || messageFirstInDialog)
                    h.showMessageSender(showPhoto)
                    if (showPhoto)
                        loadUserImage(h.messageSenderPhoto, message.senderOrEmpty().photoUrl)
                }
                TYPE_OUT -> {
                }
            }

            // Base data
            baseHolder.clearMessageAttachments()
            attachmentGenerator.inflate(message.attachments, inflater, baseHolder.messageAttachments) forEach {
                baseHolder addAttachmentToMessage it
            }
            baseHolder.setMessageText(message.text)
            baseHolder.setMessageDate(
                    if (message.sentState == Message.STATE_SENDING)
                        ""
                    else
                        DateFormat.time(message.sentTimeMillis / 1000L)
            )
            baseHolder.showTriangle(messageFirstInChain || messageFirstInDay || messageFirstInDialog)
            if (message.sentState == Message.STATE_SENDING) {
                if (position == 0 || messages[position - 1].sentState != Message.STATE_SENDING
                        && !isSameDay(messages[position - 1].sentTimeMillis, System.currentTimeMillis())) {
                    baseHolder.showStrip(true)
                    baseHolder.setStripText(DateFormat.messageListDayContainer(message.sentTimeMillis))
                } else
                    baseHolder.showStrip(false)
            } else {
                if (messageFirstInDay || messageFirstInDialog) {
                    baseHolder.showStrip(true)
                    baseHolder.setStripText(DateFormat.messageListDayContainer(message.sentTimeMillis))
                } else
                    baseHolder.showStrip(false)
            }

            // Unread spaces colors and visibility
            val messageUnread = isUnreadColorVisible(message)
            baseHolder.setUnreadCommon(messageUnread)
            when {
                (message.sentState == Message.STATE_SENDING
                        || !messageFirstInDay && !messageFirstInDialog) && !messageFirstInChain -> {
                    baseHolder.setUnreadAboveMessage(show = false)
                }
                message.sentState == Message.STATE_SENDING && messageFirstInChain -> {
                    baseHolder.setUnreadAboveMessage(show = true, unread = false)
                }
                !messageUnread -> {
                    baseHolder.setUnreadAboveMessage(show = true, unread = false)
                }
                messageFirstInDialog || messageFirstInDay -> {
                    baseHolder.setUnreadAboveMessage(show = true, unread = true)
                }
                messageFirstInChain && position == 0 -> {
                    baseHolder.setUnreadAboveMessage(show = true, unread = false)
                }
                messageFirstInChain -> {
                    val prevMessageUnread = isUnreadColorVisible(messages[position - 1])
                    baseHolder.setUnreadAboveMessage(show = true, unread = prevMessageUnread)
                }
            }
            if (messageFirstInDay || messageFirstInDialog) {
                when {
                    position == 0 -> {
                        baseHolder.setUnreadAboveStrip(false)
                    }
                    else -> {
                        val prevMessageUnread = isUnreadColorVisible(messages[position - 1])
                        baseHolder.setUnreadAboveStrip(prevMessageUnread)
                    }
                }
            }

            // Read animation of this message
            val duration = READ_DURATION
            val offset = if (message.isIn) READ_OFFSET else 0L
            val isSpaceAboveMessage = baseHolder.isUnreadAboveMessageShown()
            if (readAnimationMessages remove message) {
                readAnimationStartTime.put(message, System.currentTimeMillis())
                Handler().postDelayed({ readAnimationStartTime remove message }, duration + offset)
            }
            if (readAnimationStartTime contains message) {
                val startTime = readAnimationStartTime get message
                val timeFromAnimationStart = System.currentTimeMillis() - startTime
                baseHolder.animateReadingCommon(duration, offset, timeFromAnimationStart)
                if (isSpaceAboveMessage)
                    baseHolder.animateReadingAboveMessage(duration, offset, timeFromAnimationStart)
            }

            // Read animation of prev message
            if (messageFirstInDay && position > 0) {
                val prevMessage = messages[position - 1]
                val prevDuration = READ_DURATION
                val prevOffset = if (prevMessage.isIn) READ_OFFSET else 0L
                if (readAnimationMessages contains prevMessage) {
                    baseHolder.animateReadingAboveStrip(prevDuration, prevOffset)
                } else if (readAnimationStartTime contains prevMessage) {
                    val startTime = readAnimationStartTime get prevMessage
                    val timeFromAnimationStart = System.currentTimeMillis() - startTime
                    baseHolder.animateReadingAboveStrip(prevDuration, prevOffset, timeFromAnimationStart)
                }
            }
        }
    }

    override fun onAddNewMessages(messages: Collection<Message>) {
        var maxIndex = -1
        messages forEach {
            if (it.sentState == Message.STATE_SENT) {
                val messageId = it.sentId
                val index = 1 + (this.messages indexOfLast { it.sentState == Message.STATE_SENT && it.sentId < messageId })
                this.messages.add(index, it)
                notifyItemInserted(index)
                if (index > maxIndex)
                    maxIndex = index
            } else {
                this.messages add it
                val index = this.messages.count() - 1
                notifyItemInserted(index)
                if (index > maxIndex)
                    maxIndex = index
            }
        }
        if (maxIndex != -1)
            notifyItemChanged(maxIndex + 1)
        readIncomeMessages()
        updateAnimationTime()
    }

    override fun onAddOldMessages(messages: Collection<Message>) {
        messages.reverse() forEach {
            this.messages.add(0, it)
            notifyItemInserted(0)
        }
        notifyItemChanged(messages.count())
    }

    override fun onReplaceMessage(old: Message, new: Message) {
        val index = messages indexOfFirst { it.sentId == old.sentId }
        messages.set(index, new)
        readIncomeMessages()
        notifyItemChanged(index)
        notifyItemChanged(index + 1)
        updateAnimationTime()
    }

    override fun onUpdateMessages(messages: Collection<Message>) {
        val updatedIndexes = messages map { this.messages.indexOf(it) }
        updatedIndexes forEach {
            notifyItemChanged(it)
            if (!updatedIndexes.contains(it + 1))
                notifyItemChanged(it + 1)
        }
        readIncomeMessages()
        updateAnimationTime()
    }

    override fun onReadMessages(messages: Collection<Message>) {
        val work = Runnable {
            readAnimationMessages addAll messages
            notifyDataSetChanged()
            readIncomeMessages()
        }
        val time = System.currentTimeMillis()
        if (time - lastAnimationStartTime < TIME_FOR_ANIMATION)
            Handler().postDelayed(work, TIME_FOR_ANIMATION + lastAnimationStartTime - time)
        else
            work.run()
    }

    private fun isUnreadColorVisible(msg: Message): Boolean {
        return msg.sentState != Message.STATE_SENDING
                && (msg.isNotRead
                || readAnimationMessages contains msg
                || readAnimationStartTime containsKey msg)
    }
    private fun isFirstReply(pos: Int): Boolean {
        if (pos == 0)
            return true
        val cur = messages[pos]
        val prev = messages[pos - 1]
        return when {
            cur.isOut != prev.isOut -> true
            cur.senderId != prev.senderId -> true
            else -> false
        }
    }
    private fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val calendar = Calendar.getInstance()
        if (Math.abs(millis1 - millis2) > 3600000 * 24) return false
        calendar.setTimeInMillis(millis1)
        val d1 = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.setTimeInMillis(millis2)
        val d2 = calendar.get(Calendar.DAY_OF_YEAR)
        return d1 == d2
    }
    private fun loadUserImage(view: ImageView, url: String) {
        val conf = if (url !in shownImages) {
            shownImages add url
            ImageLoadConf.loadUser
        } else {
            ImageLoadConf.loadUserWithoutAnim
        }
        ImageLoader.getInstance().displayImage(url, view, conf)
    }
    private fun updateAnimationTime() { lastAnimationStartTime = System.currentTimeMillis() }
    private fun readIncomeMessages() {
        if (messages any { it.isIn && it.isNotRead })
            RequestControl addBackground RequestReadMessages(dialogId, isChat)
    }
}