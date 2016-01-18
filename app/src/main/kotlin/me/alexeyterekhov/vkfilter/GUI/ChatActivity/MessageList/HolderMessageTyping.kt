package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import me.alexeyterekhov.vkfilter.GUI.Common.AnimationUtil
import me.alexeyterekhov.vkfilter.R

class HolderMessageTyping(view: View) : RecyclerView.ViewHolder(view) {
    val icon = view.findViewById(R.id.typingIcon) as ImageView
    val typingText = view.findViewById(R.id.typingText) as TextView

    fun animate() {
        AnimationUtil.typingAnimationWhileVisible(icon)
    }
}