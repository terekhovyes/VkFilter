package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.view.View
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext

class HolderMessageOut(view: View): HolderMessageBase(view) {
    // Colors
    fun setColorsForState(messageState: Int) {
        when (messageState) {
            Message.STATE_SENDING -> {
                messageBody setBackgroundResource R.drawable.message_sending_background
                messageTriangle setBackgroundResource R.drawable.message_sending_triangle
                messageText setTextColor AppContext.instance.getResources().getColor(R.color.ui_msg_font_sending)
            }
            Message.STATE_SENT -> {
                messageBody setBackgroundResource R.drawable.message_out_background
                messageTriangle setBackgroundResource R.drawable.message_out_triangle
                messageText setTextColor AppContext.instance.getResources().getColor(R.color.ui_msg_font)
            }
        }
    }
    fun setColorsSelected() {
        messageBody setBackgroundResource R.drawable.message_sel_background
        messageTriangle setBackgroundResource R.drawable.message_out_sel_triangle
        messageText setTextColor AppContext.instance.getResources().getColor(R.color.ui_msg_font)
    }
}