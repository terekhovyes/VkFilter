package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.DateFormat
import me.alexeyterekhov.vkfilter.Util.TextFormat

class ForwardMessageHolder(val view: View) {
    val senderPhoto = view.findViewById(R.id.senderPhoto) as ImageView
    val senderName = view.findViewById(R.id.senderName) as TextView
    val date = view.findViewById(R.id.date) as TextView
    val messageText = view.findViewById(R.id.messageText) as TextView
    val attachmentsLayout = view.findViewById(R.id.attachmentsLayout) as LinearLayout

    // Base info
    fun fillUserInfo(user: User) {
        val loader = ImageLoader.getInstance()
        loader.displayImage(user.photoUrl, senderPhoto)
        senderName setText TextFormat.userTitle(user)
    }
    fun fillUserNotLoaded() {
        senderPhoto setImageResource R.drawable.stub_user
        senderName setText ""
    }
    fun setDate(msc: Long) {
        date setText DateFormat.forwardMessageDate(msc)
    }
    fun setMessageText(text: String) {
        if (text == "")
            messageText setVisibility View.GONE
        else
            messageText setText text
    }

    // Fill attachments and forward messages
    fun addAttachment(view: View) {
        attachmentsLayout.addView(view)
    }
}