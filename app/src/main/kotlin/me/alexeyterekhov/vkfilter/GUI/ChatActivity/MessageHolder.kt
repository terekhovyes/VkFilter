package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import me.alexeyterekhov.vkfilter.R

/**
 * Created by Alexey on 13.08.2014.
 */

class IncomeMessageHolder (val view: View) {
    val senderPhoto = view.findViewById(R.id.senderPhoto) as ImageView
    val date = view.findViewById(R.id.date) as TextView
    val messageContainer = view.findViewById(R.id.messageContainer) as LinearLayout
    val messageText = view.findViewById(R.id.messageText) as TextView
    val messageDay = view.findViewById(R.id.messageDay) as TextView
    val messageDayLayout = view.findViewById(R.id.messageDayLayout) as LinearLayout
    val unreadBackground = view.findViewById(R.id.messageBack) as ImageView
    val topMargin = view.findViewById(R.id.messageTopMargin)
    val leftMargin = view.findViewById(R.id.messageLeftMargin)
}

class OutcomeMessageHolder (val view: View) {
    val date = view.findViewById(R.id.date) as TextView
    val messageContainer = view.findViewById(R.id.messageContainer) as LinearLayout
    val messageText = view.findViewById(R.id.messageText) as TextView
    val messageDay = view.findViewById(R.id.messageDay) as TextView
    val messageDayLayout = view.findViewById(R.id.messageDayLayout) as LinearLayout
    val unreadBackground = view.findViewById(R.id.messageBack) as ImageView
    val topMargin = view.findViewById(R.id.messageTopMargin)
}