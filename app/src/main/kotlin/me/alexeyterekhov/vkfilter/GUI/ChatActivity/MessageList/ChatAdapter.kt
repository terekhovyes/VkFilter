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


public class ChatAdapter(
        val dialogId: String,
        val isChat: Boolean,
        val activity: AppCompatActivity
):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        MessageCacheListener
{
    val TYPE_IN = 1
    val TYPE_OUT = 2
    val TYPE_FOOTER = 3

    val TIME_FOR_ANIMATION = 400L

    val selectedMessageIds = HashSet<Long>()
    var onSelectionChangeAction: (() -> Unit)? = null

    val inflater = LayoutInflater.from(activity)
    val messages = LinkedList<Message>()
    var attachmentGenerator: AttachmentsViewGenerator by Delegates.notNull()
    val shownImages = HashSet<String>()

    val messagesForReading = HashSet<Message>()
    val animationStartTime = HashMap<Message, Long>()
    var lastAnimationStartTime = 0L

    fun messagePosById(id: Long) = messages indexOfLast { it.sentId == id }
    fun messageById(id: Long) = messages last { it.sentId == id }
    fun selectMessage(id: Long) {
        selectedMessageIds add id
        notifyItemChanged(messagePosById(id))
        onSelectionChangeAction?.invoke()
    }
    fun deselectMessage(id: Long) {
        selectedMessageIds remove id
        notifyItemChanged(messagePosById(id))
        onSelectionChangeAction?.invoke()
    }
    fun deselectAllMessages() {
        val ids = HashSet(selectedMessageIds)
        selectedMessageIds.clear()
        notifyItemRangeChanged(0, messages.count())
        onSelectionChangeAction?.invoke()
    }
    fun getSelectedMessageIds() = selectedMessageIds.toSortedList()

    override fun onAddNewMessages(messages: Collection<Message>) {
        messages forEach {
            if (it.sentState == Message.STATE_SENT) {
                val messageId = it.sentId
                val index = 1 + (this.messages indexOfLast { it.sentState == Message.STATE_SENT && it.sentId < messageId })
                this.messages.add(index, it)
                notifyItemInserted(index)
            } else {
                this.messages add it
                notifyItemInserted(this.messages.count() - 1)
            }
        }
        readIncomeMessages()
        updateAnimationTime()
    }

    override fun onAddOldMessages(messages: Collection<Message>) {
        messages.reverse() forEach {
            this.messages.add(0, it)
            notifyItemInserted(0)
        }
    }

    override fun onReplaceMessage(old: Message, new: Message) {
        val index = messages indexOfFirst { it.sentId == old.sentId }
        messages.set(index, new)
        readIncomeMessages()
        notifyItemChanged(index)
        updateAnimationTime()
    }

    override fun onUpdateMessages(messages: Collection<Message>) {
        val indexes = messages map { this.messages.indexOf(it) }
        indexes forEach { notifyItemChanged(it) }
        readIncomeMessages()
        updateAnimationTime()
    }

    override fun onReadMessages(messages: Collection<Message>) {
        val work = Runnable {
            messagesForReading addAll messages
            notifyDataSetChanged()
            readIncomeMessages()
        }
        val time = System.currentTimeMillis()
        if (time - lastAnimationStartTime < TIME_FOR_ANIMATION)
            Handler().postDelayed(work, TIME_FOR_ANIMATION + lastAnimationStartTime - time)
        else
            work.run()
    }

    override fun getItemCount() = messages.count() + 1
    override fun getItemViewType(pos: Int) = when {
        pos < messages.count() && messages[pos].isOut -> TYPE_OUT
        pos < messages.count() && messages[pos].isIn -> TYPE_IN
        else -> TYPE_FOOTER
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_FOOTER) {

        } else {
            val message = messages[position]
            val isFirstReply = position == 0 || isFirstReply(position)
            val isNewDay = position > 0 && !isSameDay(message.sentTimeMillis, messages[position - 1].sentTimeMillis)
            val isVeryFirstMessage = position == 0 && MessageCaches.getCache(dialogId, isChat).historyLoaded

            val longListener = { view: View ->
                if (selectedMessageIds.isNotEmpty()) {
                    deselectAllMessages()
                } else {
                    selectMessage(message.sentId)
                    notifyItemRangeChanged(0, messages.count())
                }
                true
            }
            val shortListener = { view: View ->
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

            when (getItemViewType(position)) {
                TYPE_IN -> {
                    val h = holder as MessageInHolder
                    h.clearAttachments()
                    with (h) {
                        setMessageText(message.text)
                        setDateText(DateFormat.time(message.sentTimeMillis / 1000L))
                        when {
                            messagesForReading contains message -> {
                                messagesForReading remove message
                                animationStartTime.put(message, System.currentTimeMillis())
                                readMessage()
                                Handler().postDelayed({ animationStartTime remove message }, h.READ_OFFSET + h.READ_DURATION)
                            }
                            animationStartTime contains message -> {
                                val startTime = animationStartTime get message
                                readMessage(timeFromAnimationStart = System.currentTimeMillis() - startTime)
                            }
                            else -> {
                                setUnread(message.isNotRead)
                            }
                        }
                        showSpaceAndTriangle(isFirstReply || isNewDay)
                        showPhoto(isChat && (isFirstReply || isNewDay))
                        if (isChat && (isFirstReply || isNewDay)) loadUserImage(h.senderPhoto, message.senderOrEmpty().photoUrl)
                        if (isNewDay || isVeryFirstMessage) {
                            showRedStrip(true)
                            setRedStripText(DateFormat.messageListDayContainer(message.sentTimeMillis))
                        } else {
                            showRedStrip(false)
                        }
                        setColors(selected = selectedMessageIds contains message.sentId)
                    }
                    attachmentGenerator.inflate(message.attachments, inflater, h.attachments) forEach {
                        h addAttachment it
                    }

                    h.itemView setOnLongClickListener longListener
                    if (selectedMessageIds.isNotEmpty()) {
                        h.setTopSelectorEnabled(true)
                        h.topSelector setOnLongClickListener longListener
                        h.topSelector setOnClickListener shortListener
                    } else
                        h.setTopSelectorEnabled(false)
                }
                TYPE_OUT -> {
                    val h = holder as MessageOutHolder
                    h.clearAttachments()
                    with (h) {
                        setMessageText(message.text)
                        if (messagesForReading contains message) {
                            messagesForReading remove message
                            readMessage()
                        }
                    }
                    when (message.sentState) {
                        Message.STATE_SENT -> {
                            h.setDateText(DateFormat.time(message.sentTimeMillis / 1000L))
                            h.setUnread(!message.isRead)
                            if (isNewDay || isVeryFirstMessage) {
                                h.showRedStrip(true)
                                h.setRedStripText(DateFormat.messageListDayContainer(message.sentTimeMillis))
                            } else {
                                h.showRedStrip(false)
                            }
                            h.showSpaceAndTriangle(isFirstReply || isNewDay)
                            attachmentGenerator.inflate(message.attachments, inflater, h.attachments) forEach {
                                h addAttachment it
                            }

                            h.itemView setOnLongClickListener longListener
                            if (selectedMessageIds.isNotEmpty()) {
                                h.setSelectorEnabled(true)
                                h.topSelector setOnLongClickListener longListener
                                h.topSelector setOnClickListener shortListener
                            } else
                                h.setSelectorEnabled(false)
                        }
                        Message.STATE_PROCESSING -> {
                            h.setDateText("")
                            h.setUnread(false)
                            val showStrip = position == 0 || (messages[position - 1].sentState != Message.STATE_PROCESSING
                                    && !isSameDay(messages[position - 1].sentTimeMillis, System.currentTimeMillis()))
                            h.showRedStrip(showStrip)
                            if (showStrip)
                                h.setRedStripText(DateFormat.messageListDayContainer(System.currentTimeMillis()))
                            h.showSpaceAndTriangle(isFirstReply || showStrip)
                            attachmentGenerator.inflate(message.attachments, inflater, h.attachments, darkColors = true) forEach {
                                h addAttachment it
                            }
                            h.itemView setOnLongClickListener null
                            h.setSelectorEnabled(false)
                        }
                    }
                    if (selectedMessageIds contains message.sentId)
                        h.setColorsSelected()
                    else
                        h.setColorsByMessageState(message.sentState)
                }
            }
        }
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
            TYPE_IN -> MessageInHolder(view)
            TYPE_OUT -> MessageOutHolder(view)
            TYPE_FOOTER -> MessageFooterHolder(view)
            else -> throw Exception("WRONG ITEM TYPE")
        }
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