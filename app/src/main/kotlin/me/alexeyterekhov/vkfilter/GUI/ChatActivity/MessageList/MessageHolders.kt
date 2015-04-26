package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import me.alexeyterekhov.vkfilter.R


class OutcomeMessageHolder (val view: View) {
    val date = view.findViewById(R.id.date) as TextView
    val messageContainer = view.findViewById(R.id.messageContainer) as LinearLayout
    val messageText = view.findViewById(R.id.messageText) as TextView
    val messageDay = view.findViewById(R.id.messageDay) as TextView
    val messageDayLayout = view.findViewById(R.id.messageDayLayout) as LinearLayout
    val unreadBackground = view.findViewById(R.id.messageBack) as ImageView
    val topMargin = view.findViewById(R.id.messageTopMargin)
}