package me.alexeyterekhov.vkfilter.GUI.ChatActivity.AttachmentsList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import me.alexeyterekhov.vkfilter.R

class ImageHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val image = view.findViewById(R.id.image) as ImageView
    val progressBar = view.findViewById(R.id.progressBar) as ProgressBar
    val deleteButton = view.findViewById(R.id.deleteButton) as ImageView
}