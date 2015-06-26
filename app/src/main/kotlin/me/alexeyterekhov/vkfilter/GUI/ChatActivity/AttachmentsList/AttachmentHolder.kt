package me.alexeyterekhov.vkfilter.GUI.ChatActivity.AttachmentsList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import me.alexeyterekhov.vkfilter.GUI.Common.ImageProgressBar
import me.alexeyterekhov.vkfilter.R

class AttachmentHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val imageProgressBar = view.findViewById(R.id.imageProgressBar) as ImageProgressBar
    val countText = view.findViewById(R.id.count) as TextView
    val labelText = view.findViewById(R.id.label) as TextView
}