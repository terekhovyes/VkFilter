package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.view.LayoutInflater
import android.widget.BaseAdapter
import java.util.LinkedList
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ListView
import android.animation.Animator
import me.alexeyterekhov.vkfilter.R
import java.util.Calendar
import android.view.animation.AnimationUtils
import android.util.Log
import android.widget.FrameLayout
import java.util.Vector
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Common.DateFormat
import me.alexeyterekhov.vkfilter.DataCache.MessageCache
import android.widget.ImageView
import android.widget.LinearLayout
import java.util.HashSet
import me.alexeyterekhov.vkfilter.Common.AppContext
import android.content.Intent
import me.alexeyterekhov.vkfilter.GUI.PhotoViewerActivity.PhotoViewerActivity
import android.app.Activity
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun

class MessageListAdapter(
        val activity: Activity,
        val id: String,
        val chat: Boolean
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(AppContext.instance)

    // messages from oldest to newest
    private var messages: LinkedList<Message>? = null
    private val normalColor = AppContext.instance.getResources()!!.getColor(R.color.main_white)
    private val unreadColor = AppContext.instance.getResources()!!.getColor(R.color.light_green)
    private val calendar = Calendar.getInstance()

    private var animationCount = 0
    private var allowAppearAnimation = true // for only one invocation
    private var animateLast = 0
    private var shouldNotify = false
    public var firstLoad: Boolean = true

    private val shownImages = HashSet<String>()
    public var maxImageWidth: Int = 0
    public var maxImageHeight: Int = 0

    override fun getCount() = if (messages == null) 0 else messages!!.size
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

        val container = view.findViewById(R.id.messageContainer) as LinearLayout
        container.removeViews(1, container.getChildCount() - 1)
        for (img in msg.attachments.images) {
            val imgView = inflater.inflate(R.layout.message_image, container, false) as ImageView
            val aimRatio = maxImageWidth / maxImageHeight.toDouble()
            val imgRatio = img.width / img.height.toDouble()
            when {
                imgRatio > aimRatio -> {
                    with (imgView.getLayoutParams()) {
                        width = maxImageWidth
                        height = (maxImageWidth / imgRatio).toInt()
                    }
                }
                else -> {
                    with (imgView.getLayoutParams()) {
                        width = (maxImageHeight * imgRatio).toInt()
                        height = maxImageHeight
                    }
                }
            }
            imgView.setOnClickListener {
                val intent = Intent(AppContext.instance, javaClass<PhotoViewerActivity>())
                intent.putExtra("photo_url", img.fullSizeUrl)
                activity.startActivity(intent)
            }
            container.addView(imgView)
            loadImage(imgView, img.smallSizeUrl)
        }

        if (msg.isOut) {
            val h = holder as OutcomeMessageHolder
            with (h) {
                messageText.setText(msg.text)
                messageText.setVisibility(if (msg.text != "") View.VISIBLE else android.view.View.GONE)
                date.setText(msg.formattedDate)
                unreadBackground.setVisibility(if (msg.isRead) View.INVISIBLE else android.view.View.VISIBLE)
                changeBackground(messageContainer,
                        if (f || d) R.drawable.message_out
                        else R.drawable.message_out_compact)
                topMargin.setVisibility(if (f || d) View.VISIBLE else android.view.View.GONE)
                if (d) {
                    messageDayLayout.setVisibility(View.VISIBLE)
                    messageDay.setText(DateFormat.messageListDayContainer(msg.dateMSC))
                } else messageDayLayout.setVisibility(View.GONE)
            }
        } else {
            val h = holder as IncomeMessageHolder
            with (h) {
                messageText.setText(msg.text)
                messageText.setVisibility(if (msg.text != "") View.VISIBLE else android.view.View.GONE)
                date.setText(msg.formattedDate)
                unreadBackground.setVisibility(if (msg.isRead) View.INVISIBLE else android.view.View.VISIBLE)
                changeBackground(messageContainer,
                        if (f || d) R.drawable.message_in
                        else R.drawable.message_in_compact)
                leftMargin.setVisibility(
                        if (chat && !(f || d)) View.VISIBLE
                        else View.GONE)
                topMargin.setVisibility(if (f || d) View.VISIBLE else android.view.View.GONE)
                if (d) {
                    messageDayLayout.setVisibility(View.VISIBLE)
                    messageDay.setText(DateFormat.messageListDayContainer(msg.dateMSC))
                } else messageDayLayout.setVisibility(View.GONE)
            }
            if (chat && (f || d)) {
                with (h.senderPhoto) {
                    setVisibility(View.VISIBLE)
                    loadImage(this, msg.sender.photoUrl)
                }
            } else
                h.senderPhoto.setVisibility(View.GONE)
        }

        // Animation
        if (allowAppearAnimation
                && animateLast > 0
                && position == messages!!.size - animateLast) {
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
    private fun loadImage(view: ImageView, url: String) {
        val options = DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(R.drawable.user_photo_loading)
                .showImageOnFail(R.drawable.user_photo_loading)
        if (url !in shownImages) {
            shownImages add url
            ImageLoader.getInstance().displayImage(url, view, options
                            .displayer(FadeInBitmapDisplayer(150))
                            .build())
        } else {
            ImageLoader.getInstance().displayImage(url, view, options.build())
        }
    }

    private fun changeBackground(view: View, res: Int) {
        val bottom = view.getPaddingBottom()
        val top = view.getPaddingTop()
        val right = view.getPaddingRight()
        val left = view.getPaddingLeft()
        view.setBackgroundResource(res)
        view.setPadding(left, top, right, bottom)
    }

    private fun getInView(parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.message_income, parent, false)!!
        view.setTag(IncomeMessageHolder(view))
        return view
    }

    private fun getOutView(parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.message_outcome, parent, false)!!
        view.setTag(OutcomeMessageHolder(view))
        return view
    }

    private fun isFirstReply(pos: Int)
        = pos > 0 && (
            messages!![pos].isOut && !messages!![pos - 1].isOut ||
            !messages!![pos].isOut && messages!![pos - 1].sender != messages!![pos].sender)

    private fun isSameDay(date1: Long, date2: Long): Boolean {
        if (Math.abs(date1 - date2) > 3600000 * 24) return false
        calendar.setTimeInMillis(date1)
        val d1 = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.setTimeInMillis(date2)
        val d2 = calendar.get(Calendar.DAY_OF_YEAR)
        return d1 == d2
    }

    fun notifyOnNewMessages(listView: ListView) {
        allowAppearAnimation = false
        val messagePack = MessageCache.getDialog(id, chat)
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
                val newSize = messages!!.size
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
            RunFun.markIncomesAsRead(id, chat)
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
                    val alreadyRead = if (h is IncomeMessageHolder) h.unreadBackground.getVisibility() == View.INVISIBLE
                    else (h as OutcomeMessageHolder).unreadBackground.getVisibility() == View.INVISIBLE
                    if (!alreadyRead) {
                        val a = AnimationUtils.loadAnimation(AppContext.instance, R.anim.message_read)
                        val l = object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {
                            }

                            override fun onAnimationEnd(animation: Animation?) {
                                if (h is IncomeMessageHolder)
                                    h.unreadBackground.setVisibility(View.INVISIBLE)
                                else
                                    (h as OutcomeMessageHolder).unreadBackground.setVisibility(View.INVISIBLE)
                            }

                            override fun onAnimationRepeat(animation: Animation?) {
                            }
                        }
                        a!!.setAnimationListener(l)
                        if (h is IncomeMessageHolder)
                            h.unreadBackground.startAnimation(a)
                        else
                            (h as OutcomeMessageHolder).unreadBackground.startAnimation(a)
                    }
                }
            }
        }
    }
}