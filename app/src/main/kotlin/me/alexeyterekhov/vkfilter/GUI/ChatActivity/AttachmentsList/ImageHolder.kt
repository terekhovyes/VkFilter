package me.alexeyterekhov.vkfilter.GUI.ChatActivity.AttachmentsList

import android.support.v7.widget.RecyclerView
import android.view.View
import me.alexeyterekhov.vkfilter.GUI.Common.ImageProgressBar
import me.alexeyterekhov.vkfilter.R

class ImageHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val imageProgressBar = view.findViewById(R.id.imageProgressBar) as ImageProgressBar
}