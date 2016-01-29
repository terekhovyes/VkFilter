package me.alexeyterekhov.vkfilter.GUI.DialogsActivity.DialogList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.R


class DialogHolder(dialogView: View): RecyclerView.ViewHolder(dialogView) {
    // Images
    private var imageCount = 1
    val singleImage = dialogView.findViewById(R.id.singleDialogIcon) as ImageView
    val doubleLayout = dialogView.findViewById(R.id.doubleDialogIconLayout) as RelativeLayout
    val doubleImages = arrayOf(
            dialogView.findViewById(R.id.doubleDialogIcon1) as ImageView,
            dialogView.findViewById(R.id.doubleDialogIcon2) as ImageView
    )
    val tripleLayout = dialogView.findViewById(R.id.tripleDialogIconLayout) as RelativeLayout
    val tripleImages = arrayOf(
            dialogView.findViewById(R.id.tripleDialogIcon1) as ImageView,
            dialogView.findViewById(R.id.tripleDialogIcon2) as ImageView,
            dialogView.findViewById(R.id.tripleDialogIcon3) as ImageView
    )
    val quadLayout = dialogView.findViewById(R.id.quadDialogIconLayout) as RelativeLayout
    val quadImages = arrayOf(
            dialogView.findViewById(R.id.quadDialogIcon1) as ImageView,
            dialogView.findViewById(R.id.quadDialogIcon2) as ImageView,
            dialogView.findViewById(R.id.quadDialogIcon3) as ImageView,
            dialogView.findViewById(R.id.quadDialogIcon4) as ImageView
    )

    // Content
    val title = dialogView.findViewById(R.id.dialogName) as TextView
    val onlineIcon = dialogView.findViewById(R.id.dialogOnline) as ImageView
    val messageDate = dialogView.findViewById(R.id.dialogDate) as TextView
    val messageImage = dialogView.findViewById(R.id.dialogSenderIcon) as ImageView
    val messageTypingImage = dialogView.findViewById(R.id.typingIcon) as ImageView
    val messageText = dialogView.findViewById(R.id.dialogLastMessage) as TextView
    val unreadBackground = dialogView.findViewById(R.id.unreadBackground) as ImageView
    val unreadMessage = dialogView.findViewById(R.id.unreadMessage) as ImageView
    val attachmentIcon = dialogView.findViewById(R.id.attachmentIcon) as ImageView

    fun chooseImageLayoutForImageCount(count: Int) {
        imageCount = count
        singleImage.visibility = if (count == 1) View.VISIBLE else View.INVISIBLE
        doubleLayout.visibility = if (count == 2) View.VISIBLE else View.INVISIBLE
        tripleLayout.visibility = if (count == 3) View.VISIBLE else View.INVISIBLE
        quadLayout.visibility = if (count == 4) View.VISIBLE else View.INVISIBLE
    }

    fun getImageView(position: Int) = when (imageCount) {
        1 -> singleImage
        2 -> doubleImages[position]
        3 -> tripleImages[position]
        else -> quadImages[position]
    }

    fun setAttachmentIcon(m: Message) {
        val a = m.attachments
        val icons = hashMapOf(
                a.audios to R.drawable.attachment_audio,
                a.images to R.drawable.attachment_image,
                a.videos to R.drawable.attachment_video,
                a.documents to R.drawable.attachment_document,
                a.messages to R.drawable.attachment_message,
                a.walls to R.drawable.attachment_wall,
                a.links to R.drawable.attachment_link
        )
        when {
            icons.keys.all { it.isEmpty() } -> {
                attachmentIcon.visibility = View.GONE
            }
            icons.keys.count { it.isNotEmpty() } > 1 -> {
                attachmentIcon.visibility = View.VISIBLE
                attachmentIcon.setImageResource(R.drawable.attachment_common)
            }
            else -> {
                attachmentIcon.visibility = View.VISIBLE
                val col = icons.keys.first { it.isNotEmpty() }
                attachmentIcon.setImageResource(icons.get(col)!!)
            }
        }
    }
}