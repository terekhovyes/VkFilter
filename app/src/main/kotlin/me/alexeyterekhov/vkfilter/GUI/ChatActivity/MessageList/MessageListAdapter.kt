package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ListView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.MessageCacheOld
import me.alexeyterekhov.vkfilter.DataClasses.MessageOld
import me.alexeyterekhov.vkfilter.InternetOld.VkApi.RunFun
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DateFormat
import me.alexeyterekhov.vkfilter.Util.ImageLoadConf
import java.util.Calendar
import java.util.HashSet
import java.util.LinkedList
import java.util.Vector

class MessageListAdapter(
        val activity: AppCompatActivity,
        val id: String,
        val chat: Boolean
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(activity)

    // messages from oldest to newest
    private var messages: LinkedList<MessageOld>? = null
    private val normalColor = AppContext.instance.getResources()!!.getColor(R.color.my_white)
    private val unreadColor = AppContext.instance.getResources()!!.getColor(R.color.my_green_lighter)
    private val calendar = Calendar.getInstance()

    private var animationCount = 0
    private var allowAppearAnimation = true // for only one invocation
    private var animateLast = 0
    private var shouldNotify = false
    public var firstLoad: Boolean = true

    private val shownImages = HashSet<String>()
    public var maxImageWidth: Int = 0
        set(w) {
            $maxImageWidth = w
            attachmentGenerator = createAttachmentGenerator()
        }
    public var maxImageHeight: Int = 0
        set(h) {
            $maxImageHeight = h
            attachmentGenerator = createAttachmentGenerator()
        }

    private var attachmentGenerator = createAttachmentGenerator()

    override fun getCount() = if (messages == null) 0 else messages!!.size()
    override fun getItem(position: Int) = messages!![position]
    override fun getItemId(position: Int) = position.toLong()
    override fun getViewTypeCount() = 2
    override fun getItemViewType(position: Int) = if (messages!![position].isOut) 0 else 1
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val msg = messages!![position]
        val f = position == 0 || isFirstReply(position)
        val d = position > 0 && !isSameDay(msg.dateMSC, messages!![position - 1].dateMSC)
        val view = when {
            convertView != null -> convertView
            msg.isOut -> getOutView(parent)
            else -> getInView(parent)
        }
        val holder = view!!.getTag()

        if (msg.isOut) {
            val h = holder as MessageOutHolder
            h.clearAttachments()
            with (h) {
                setText(msg.text)
                setDate(DateFormat.time(msg.dateMSC / 1000L))
                setUnread(!msg.isRead)
                firstMessage(f || d)
                showRedStrip(d)
                if (d) setRedStripText(DateFormat.messageListDayContainer(msg.dateMSC))
            }
            attachmentGenerator.inflate(msg.attachments, inflater, h.attachments) forEach {
                h addAttachment  it
            }
        } else {
            val h = holder as MessageInHolder
            h.clearAttachments()
            with (h) {
                setText(msg.text)
                setDate(DateFormat.time(msg.dateMSC / 1000L))
                setUnread(!msg.isRead)
                firstMessage(f || d)
                showPhoto(chat && (f || d))
                if (chat && (f || d)) loadUserImage(h.senderPhoto, msg.senderOrEmpty().photoUrl)
                showRedStrip(d)
                if (d) setRedStripText(DateFormat.messageListDayContainer(msg.dateMSC))
            }
            attachmentGenerator.inflate(msg.attachments, inflater, h.attachments) forEach {
                h addAttachment it
            }
        }

        // Animation
        if (allowAppearAnimation
                && animateLast > 0
                && position == messages!!.size() - animateLast) {
            val a = AnimationUtils.loadAnimation(AppContext.instance, R.anim.message_appear)!!
            a.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    ++animationCount
                    Log.d("message list", "start alpha anim")
                    --animateLast
                }
                override fun onAnimationEnd(animation: Animation?) {
                    Log.d("message list", "stop alpha anim")
                    --animationCount
                    if (shouldNotify) notifyWhenPossible()
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            view.startAnimation(a)
            Log.d("message list", "invoke alpha animation")
        }
        if (firstLoad) {
            val a = AnimationUtils.loadAnimation(AppContext.instance, R.anim.message_appear)!!
            a.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    ++animationCount
                    view setVisibility View.VISIBLE
                }
                override fun onAnimationEnd(animation: Animation?) {
                    firstLoad = false
                    --animationCount
                    if (shouldNotify) notifyWhenPossible()
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            view setVisibility View.INVISIBLE
            view.startAnimation(a)
        }
        return view
    }

    fun notifyWhenPossible() {
        if (animationCount == 0) {
            notifyDataSetChanged()
            Log.d("message list", "notify from when possible")
            shouldNotify = false
        } else shouldNotify = true
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

    private fun getInView(parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.message_in, parent, false)
        view.setTag(MessageInHolder(view))
        return view
    }

    private fun getOutView(parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.message_out, parent, false)!!
        view.setTag(MessageOutHolder(view))
        return view
    }

    private fun isFirstReply(pos: Int): Boolean {
        if (pos == 0)
            return true
        val cur = messages!![pos]
        val prev = messages!![pos - 1]
        return when {
            cur.isOut != prev.isOut -> true
            cur.senderId != prev.senderId -> true
            else -> false
        }
    }

    private fun isSameDay(date1: Long, date2: Long): Boolean {
        if (Math.abs(date1 - date2) > 3600000 * 24) return false
        calendar.setTimeInMillis(date1)
        val d1 = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.setTimeInMillis(date2)
        val d2 = calendar.get(Calendar.DAY_OF_YEAR)
        return d1 == d2
    }

    private fun createAttachmentGenerator() = AttachmentsViewGenerator(
            maxImageWidth,
            maxImageHeight,
            shownImages,
            activity
    )

    fun notifyOnNewMessages(listView: ListView) {
        allowAppearAnimation = false
        val messagePack = MessageCacheOld.getDialog(id, chat)
        when {
            messages == null -> {
                messages = messagePack.messages
                notifyWhenPossible()
                allowAppearAnimation = true
            }
            !messagePack.info.addedMessagesAreNew -> {
                // Save and restore position
                val pos = listView.getFirstVisiblePosition()
                val view = listView.getChildAt(0)
                val top = if (view == null) 0 else view.getTop()
                notifyWhenPossible()
                listView.setSelectionFromTop(pos + messagePack.info.addedMessagesCount, top)
                allowAppearAnimation = true
            }
            messagePack.info.addedMessagesAreNew -> {
                val pos = listView.getLastVisiblePosition()
                val newSize = messages!!.size()
                val scrollPos = newSize - 1 - messagePack.info.addedMessagesCount
                var scroll = scrollPos == pos
                if (scroll) {
                    val v = listView.getChildAt(listView.getChildCount() - 1)
                    if (v != null) {
                        scroll = v.getTop() + v.getHeight() - listView.getHeight() < 100
                    }
                }
                if (scroll) {
                    val group = FrameLayout(AppContext.instance)
                    val newItems = messagePack.info.addedMessagesCount
                    animateLast = newItems
                    var heightOfNewItems = 0
                    for (i in newSize - newItems .. newSize - 1) {
                        val view = getView(i, null, group) as View
                        view.measure(0, 0)
                        heightOfNewItems += view.getMeasuredHeight()
                    }
                    val visibleCount
                            = with (listView) { getLastVisiblePosition() - getFirstVisiblePosition() + 1 }
                    val animators = Vector<Animator>()
                    for (i in 0 .. visibleCount - 1) {
                        val view = listView.getChildAt(i)
                        val top = view!!.getTop()
                        val animator = ObjectAnimator.ofInt(view, "top", top, top - heightOfNewItems)
                        animators.add(animator)
                    }
                    val set = AnimatorSet()
                    set.setDuration(200)
                    set.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {
                            ++animationCount
                            Log.d("message list", "start translation anim")
                        }
                        override fun onAnimationEnd(animation: Animator) {
                            --animationCount
                            allowAppearAnimation = true
                            Log.d("message list", "stop translation anim")
                            notifyDataSetChanged()
                            Log.d("message list", "notify from translation end")
                            shouldNotify = false
                            listView.setSelection(newSize - 1)
                        }
                        override fun onAnimationCancel(animation: Animator?) {}
                        override fun onAnimationRepeat(animation: Animator?) {}
                    })
                    set.playTogether(animators)
                    set.start()
                } else {
                    notifyWhenPossible()
                    allowAppearAnimation = true
                }
            }
            else -> {
                notifyWhenPossible()
                allowAppearAnimation = true
            }
        }
        if (messagePack.messages any { !it.isOut && !it.isRead })
            RunFun.markIncomesAsReadOld(id, chat)
    }

    fun markAsRead(
            listView: ListView,
            fromId: Long,
            toId: Long,
            incomes: Boolean
    ) {
        if (messages == null) return
        val firstPos = listView.getFirstVisiblePosition()
        val lastPos = listView.getLastVisiblePosition()

        for (i in firstPos..lastPos) {
            val msg = messages!![i]
            if (msg.isOut != incomes && msg.id in fromId..toId) {
                val view = listView.getChildAt(i - firstPos)
                if (view != null) {
                    val h = view.getTag()
                    val alreadyRead = if (h is MessageInHolder) h.isRead()
                    else (h as MessageOutHolder).isRead()
                    if (!alreadyRead) {
                        val a = AnimationUtils.loadAnimation(AppContext.instance, R.anim.message_read)
                        val l = object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {
                            }

                            override fun onAnimationEnd(animation: Animation?) {
                                if (h is MessageInHolder)
                                    h.setUnread(false)
                                else
                                    (h as MessageOutHolder).setUnread(false)
                            }

                            override fun onAnimationRepeat(animation: Animation?) {
                            }
                        }
                        a!!.setAnimationListener(l)
                        if (h is MessageInHolder)
                            h.unreadBackground.startAnimation(a)
                        else
                            (h as MessageOutHolder).unreadBackground.startAnimation(a)
                    }
                }
            }
        }
    }
}