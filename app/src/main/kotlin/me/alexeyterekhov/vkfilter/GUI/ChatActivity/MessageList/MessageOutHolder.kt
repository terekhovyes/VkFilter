package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import me.alexeyterekhov.vkfilter.R

class MessageOutHolder(view: View) {
    // Data
    val date = view.findViewById(R.id.date) as TextView
    val messageText = view.findViewById(R.id.messageText) as TextView
    val attachments = view.findViewById(R.id.attachmentsLayout) as LinearLayout
    val forwardMessages = view.findViewById(R.id.forwardMessagesLayout) as LinearLayout
    // Additional Elements
    val messageDay = view.findViewById(R.id.messageDay) as TextView
    val messageDayLayout = view.findViewById(R.id.messageDayLayout) as LinearLayout
    // Variations
    val unreadBackground = view.findViewById(R.id.messageBack) as ImageView
    val spaceAboveMessage = view.findViewById(R.id.spaceAbove) as Space
    val triangle = view.findViewById(R.id.messageTriangle)

    fun isRead() = unreadBackground.getVisibility() != View.VISIBLE

    fun clearIncludes() {
        attachments.removeAllViews()
        forwardMessages.removeAllViews()
    }
    fun addAttachment(v: View) = attachments addView v
    fun addForwardMessage(v: View) = forwardMessages addView v
    fun setText(t: CharSequence) {
        if (t.length() == 0)
            messageText setVisibility View.GONE
        else {
            messageText setText t
            messageText setVisibility View.VISIBLE
        }
    }
    fun setDate(d: String) = date setText d
    fun setUnread(unread: Boolean) {
        unreadBackground setVisibility if (unread)
            View.VISIBLE
        else
            View.INVISIBLE
    }
    fun firstMessage(first: Boolean) {
        spaceAboveMessage setVisibility if (first) View.VISIBLE else View.GONE
        triangle setVisibility if (first) View.VISIBLE else View.INVISIBLE
    }
    fun showRedStrip(show: Boolean) {
        messageDayLayout setVisibility if (show) View.VISIBLE else View.GONE
    }
    fun setRedStripText(day: String) = messageDay setText day
}