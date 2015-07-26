package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.view.View
import android.widget.ImageView
import me.alexeyterekhov.vkfilter.R

class HolderMessageIn(view: View): HolderMessageBase(view) {
    val messageSenderPhoto = view.findViewById(R.id.messageSenderPhoto) as ImageView

    fun showMessageSender(show: Boolean) = messageSenderPhoto setVisibility if (show) View.VISIBLE else View.GONE
    fun setColors(selected: Boolean) {
        if (selected) {
            messageBody setBackgroundResource R.drawable.message_sel_background
            messageTriangle setBackgroundResource R.drawable.message_in_sel_triangle
        } else {
            messageBody setBackgroundResource R.drawable.message_in_background
            messageTriangle setBackgroundResource R.drawable.message_in_triangle
        }
    }
}