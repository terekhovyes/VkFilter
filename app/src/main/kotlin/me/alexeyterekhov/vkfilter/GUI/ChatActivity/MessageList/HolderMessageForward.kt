package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DateFormat
import me.alexeyterekhov.vkfilter.Util.TextFormat

class HolderMessageForward(val view: View) {
    val messageText = view.findViewById(R.id.messageText) as TextView
    val dateText = view.findViewById(R.id.date) as TextView
    val senderName = view.findViewById(R.id.senderName) as TextView
    val senderPhoto = view.findViewById(R.id.senderPhoto) as ImageView
    val attachmentsLayout = view.findViewById(R.id.attachmentsLayout) as LinearLayout

    // Base info
    fun fillUserInfo(user: User) {
        val loader = ImageLoader.getInstance()
        loader.displayImage(user.photoUrl, senderPhoto)
        senderName.setText(TextFormat.userTitle(user))
    }
    fun fillUserNotLoaded() {
        senderPhoto.setImageResource(R.drawable.icon_user_stub)
        senderName.setText("")
    }
    fun setDate(msc: Long) {
        dateText.setText(DateFormat.forwardMessageDate(msc))
    }
    fun setMessageText(text: String) {
        if (text == "")
            messageText.setVisibility(View.GONE)
        else
            messageText.setText(text)
    }

    // Fill attachments and forward messages
    fun addAttachment(view: View) {
        attachmentsLayout.addView(view)
    }

    fun setDarkColors() {
        senderName.setTextColor(AppContext.instance.getResources().getColor(R.color.font_dark_secondary))
        dateText.setTextColor(AppContext.instance.getResources().getColor(R.color.font_dark_tertiary))
        messageText.setTextColor(AppContext.instance.getResources().getColor(R.color.font_dark))
    }
}