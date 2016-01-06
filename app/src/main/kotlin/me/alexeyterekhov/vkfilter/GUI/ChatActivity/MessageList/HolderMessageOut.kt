package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.view.View
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.R

class HolderMessageOut(view: View): HolderMessageBase(view) {
    val sendingTint = view.findViewById(R.id.messageSendingTint)

    // Colors
    fun setColorsForState(messageState: Int) {
        sendingTint.visibility = if (messageState == Message.STATE_SENDING)
            View.VISIBLE
        else
            View.GONE
        messageBody.setBackgroundResource(R.drawable.message_out_background)
        messageTriangle.setBackgroundResource(R.drawable.message_out_triangle)
    }
    fun setColorsSelected() {
        messageBody.setBackgroundResource(R.drawable.message_sel_background)
        messageTriangle.setBackgroundResource(R.drawable.message_out_sel_triangle)
    }
}