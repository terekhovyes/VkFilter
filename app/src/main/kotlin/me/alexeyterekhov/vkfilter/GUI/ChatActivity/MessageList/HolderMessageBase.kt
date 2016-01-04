package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.TextView
import me.alexeyterekhov.vkfilter.R

open class HolderMessageBase(view: View): RecyclerView.ViewHolder(view) {
    companion object {
        fun animateDisappearing(view: View, duration: Long, offset: Long, timeFromStart: Long) {
            val animationListener = object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    view.setVisibility(View.INVISIBLE)
                }
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            }

            when {
                timeFromStart >= offset + duration -> view.setVisibility(View.INVISIBLE)
                timeFromStart > offset -> {
                    val startOpacity = Math.max(0f, 1f - (timeFromStart - offset) / duration.toFloat())
                    val animation = AlphaAnimation(startOpacity, 0f)
                    val restPartOfAnimation = startOpacity
                    animation.setDuration((duration * restPartOfAnimation).toLong())
                    animation.setAnimationListener(animationListener)
                    view.startAnimation(animation)
                }
                else -> {
                    val animation = AlphaAnimation(1f, 0f)
                    animation.setStartOffset(offset - timeFromStart)
                    animation.setDuration(duration)
                    animation.setAnimationListener(animationListener)
                    view.startAnimation(animation)
                }
            }
        }
    }

    val messageText = view.findViewById(R.id.messageText) as TextView
    val messageDate = view.findViewById(R.id.messageDate) as TextView
    val messageAttachments = view.findViewById(R.id.messageAttachments) as LinearLayout
    val messageBody = view.findViewById(R.id.messageBody)
    val messageTriangle = view.findViewById(R.id.messageTriangle)
    val stripLayout = view.findViewById(R.id.messageStripLayout)
    val stripText = view.findViewById(R.id.messageStripText) as TextView
    val unreadCommon = view.findViewById(R.id.messageUnreadCommon)
    val unreadAboveStrip = view.findViewById(R.id.messageUnreadAboveStrip)
    val unreadAboveMessage = view.findViewById(R.id.messageUnreadAboveMessage)
    val selectorBack = view.findViewById(R.id.messageBackSelector)
    val selectorTop = view.findViewById(R.id.messageTopSelector)

    // Data
    fun setMessageText(t: CharSequence) {
        if (t.length == 0)
            messageText.setVisibility(View.GONE)
        else {
            messageText.setText(t)
            messageText.setVisibility(View.VISIBLE)
        }
    }
    fun setMessageDate(d: String) = messageDate.setText(d)
    fun clearMessageAttachments() = messageAttachments.removeAllViews()
    infix fun addAttachmentToMessage(v: View) = messageAttachments.addView(v)
    fun setStripText(text: String) = stripText.setText(text)

    // Visibility
    fun showStrip(show: Boolean) = stripLayout.setVisibility(if (show) View.VISIBLE else View.GONE)
    fun showTriangle(show: Boolean) = messageTriangle.setVisibility(if (show) View.VISIBLE else View.INVISIBLE)
    fun setUnreadAboveMessage(show: Boolean, unread: Boolean = false): Unit = when {
        !show -> unreadAboveMessage.setVisibility(View.GONE)
        !unread -> unreadAboveMessage.setVisibility(View.INVISIBLE)
        else -> unreadAboveMessage.setVisibility(View.VISIBLE)
    }
    fun isUnreadAboveMessageShown() = unreadAboveMessage.getVisibility() == View.VISIBLE
    fun setUnreadAboveStrip(unread: Boolean) = unreadAboveStrip.setVisibility(if (unread) View.VISIBLE else View.INVISIBLE)
    fun isUnreadAboveStripShown() = unreadAboveStrip.getVisibility() == View.VISIBLE
    fun setUnreadCommon(unread: Boolean) = unreadCommon.setVisibility(if (unread) View.VISIBLE else View.INVISIBLE)
    fun setTopSelectorClickable(clickable: Boolean) = selectorTop.setVisibility(if (clickable) View.VISIBLE else View.GONE)
    fun animateReadingCommon(duration: Long, offset: Long, timeFromStart: Long = 0L) {
        animateDisappearing(unreadCommon, duration, offset, timeFromStart)
    }
    fun animateReadingAboveMessage(duration: Long, offset: Long, timeFromStart: Long = 0L) {
        animateDisappearing(unreadAboveMessage, duration, offset, timeFromStart)
    }
    fun animateReadingAboveStrip(duration: Long, offset: Long, timeFromStart: Long = 0L) {
        animateDisappearing(unreadAboveStrip, duration, offset, timeFromStart)
    }
}