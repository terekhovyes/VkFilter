package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.Helpers.MessageCacheListener
import me.alexeyterekhov.vkfilter.DataCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestReadMessages
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
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

    val TIME_FOR_ANIMATION = 400L

    val inflater = LayoutInflater.from(activity)
    val messages = LinkedList<Message>()
    var attachmentGenerator: AttachmentsViewGenerator by Delegates.notNull()
    val shownImages = HashSet<String>()

    val messagesForReading = HashSet<Message>()
    val animationStartTime = HashMap<Message, Long>()
    var lastAnimationStartTime = 0L

    override fun onAddNewMessages(messages: Collection<Message>) {
        Log.d("debug", "ADAPTER NEW ${messages.count()}")
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
        Log.d("debug", "ADAPTER OLD ${messages.count()}")
        messages.reverse() forEach {
            this.messages.add(0, it)
            notifyItemInserted(0)
        }
    }

    override fun onReplaceMessage(old: Message, new: Message) {
        Log.d("debug", "ADAPTER REPLACE")
        val index = messages indexOfFirst { it.sentId == old.sentId }
        messages.set(index, new)
        notifyItemChanged(index)
        updateAnimationTime()
    }

    override fun onUpdateMessages(messages: Collection<Message>) {
        Log.d("debug", "ADAPTER UPDATE ${messages.count()}")
        val indexes = messages map { this.messages.indexOf(it) }
        indexes forEach { notifyItemChanged(it) }
        readIncomeMessages()
        updateAnimationTime()
    }

    override fun onReadMessages(messages: Collection<Message>) {
        val work = Runnable {
            Log.d("debug", "ADAPTER READ ${messages.count()}")
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

    override fun getItemCount() = messages.count()
    override fun getItemViewType(pos: Int) = if (messages[pos].isOut) TYPE_OUT else TYPE_IN
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val isFirstReply = position == 0 || isFirstReply(position)
        val isNewDay = position > 0 && !isSameDay(message.sentTimeMillis, messages[position - 1].sentTimeMillis)
        val isVeryFirstMessage = position == 0 && MessageCaches.getCache(dialogId, isChat).historyLoaded

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
                }
                attachmentGenerator.inflate(message.attachments, inflater, h.attachments) forEach {
                    h addAttachment it
                }
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
                    setColorsByMessageState(message.sentState)
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
                    }
                    Message.STATE_PROCESSING -> {
                        h.setDateText(AppContext.instance.getString(R.string.a_chat_sending))
                        h.setUnread(false)
                        val showStrip = position == 0 || (messages[position - 1].sentState != Message.STATE_PROCESSING
                                && !isSameDay(messages[position - 1].sentTimeMillis, System.currentTimeMillis()))
                        h.showRedStrip(showStrip)
                        if (showStrip)
                            h.setRedStripText(DateFormat.messageListDayContainer(System.currentTimeMillis()))
                        h.showSpaceAndTriangle(isFirstReply)
                    }
                }
                attachmentGenerator.inflate(message.attachments, inflater, h.attachments) forEach {
                    h addAttachment it
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        val res = when (viewType) {
            TYPE_IN -> R.layout.message_in
            TYPE_OUT -> R.layout.message_out_new
            else -> throw Exception("WRONG ITEM TYPE")
        }
        val view = inflater.inflate(res, parent, false)
        return when (viewType) {
            TYPE_IN -> MessageInHolder(view)
            TYPE_OUT -> MessageOutHolder(view)
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