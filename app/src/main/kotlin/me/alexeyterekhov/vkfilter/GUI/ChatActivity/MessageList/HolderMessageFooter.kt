package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.support.v7.widget.RecyclerView
import android.view.View
import me.alexeyterekhov.vkfilter.R

class HolderMessageFooter(view: View) : RecyclerView.ViewHolder(view) {
    val footerUnread = view.findViewById(R.id.footerUnread)

    fun showUnread(show: Boolean) = footerUnread.setVisibility(if (show) View.VISIBLE else View.GONE)
    fun animateReading(duration: Long, offset: Long, timeFromStart: Long = 0L) {
        HolderMessageBase.animateDisappearing(footerUnread, duration, offset, timeFromStart)
    }
}