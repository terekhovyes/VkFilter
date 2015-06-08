package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import me.alexeyterekhov.vkfilter.R

class MessageInHolder(view: View): RecyclerView.ViewHolder(view) {
    val READ_OFFSET = 1000L
    val READ_DURATION = 250L

    val messageText = view.findViewById(R.id.messageText) as TextView
    val dateText = view.findViewById(R.id.date) as TextView
    val senderPhoto = view.findViewById(R.id.senderPhoto) as ImageView
    val attachments = view.findViewById(R.id.attachmentsLayout) as LinearLayout
    val redStripText = view.findViewById(R.id.messageDay) as TextView
    val redStripLayout = view.findViewById(R.id.messageDayLayout) as LinearLayout
    val messageTriangle = view.findViewById(R.id.messageTriangle)
    val unreadBackground = view.findViewById(R.id.messageBack) as ImageView
    val spaceAboveMessage = view.findViewById(R.id.spaceAbove) as Space

    fun setMessageText(t: CharSequence) {
        if (t.length() == 0)
            messageText setVisibility View.GONE
        else {
            messageText setText t
            messageText setVisibility View.VISIBLE
        }
    }
    fun setDateText(d: String) = dateText setText d
    fun clearAttachments() = attachments.removeAllViews()
    fun addAttachment(v: View) = attachments addView v
    fun setRedStripText(day: String) = redStripText setText day
    fun showRedStrip(show: Boolean) = redStripLayout setVisibility if (show) View.VISIBLE else View.GONE
    fun setUnread(unread: Boolean) {
        unreadBackground setVisibility if (unread)
            View.VISIBLE
        else
            View.INVISIBLE
    }
    fun showSpaceAndTriangle(show: Boolean) {
        spaceAboveMessage setVisibility if (show) View.VISIBLE else View.GONE
        messageTriangle setVisibility if (show) View.VISIBLE else View.INVISIBLE
    }
    fun showPhoto(show: Boolean) {
        senderPhoto setVisibility if (show) View.VISIBLE else View.GONE
    }
    fun readMessage(timeFromAnimationStart: Long = 0L) {
        if (timeFromAnimationStart >= READ_OFFSET + READ_DURATION) {
            unreadBackground setVisibility View.INVISIBLE
        } else {
            unreadBackground setVisibility View.VISIBLE
            val animation = when {
                timeFromAnimationStart < READ_OFFSET -> {
                    val a = AlphaAnimation(1.0f, 0f)
                    a.setStartOffset(READ_OFFSET - timeFromAnimationStart)
                    a.setDuration(READ_DURATION)
                    a
                }
                else -> {
                    val restPart = Math.max(1.0f - (timeFromAnimationStart - READ_OFFSET) / READ_DURATION.toFloat(), 0f)
                    val a = AlphaAnimation(restPart, 0f)
                    a.setDuration((READ_DURATION * restPart).toLong())
                    a
                }
            }
            animation setAnimationListener object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    unreadBackground setVisibility View.INVISIBLE
                }
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            }
            unreadBackground startAnimation animation
        }
    }
    fun isRead() = unreadBackground.getVisibility() != View.VISIBLE
}