package me.alexeyterekhov.vkfilter.GUI.DialogListActivity.DialogList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import me.alexeyterekhov.vkfilter.R

class DialogHolder(val dialogView: View): RecyclerView.ViewHolder(dialogView) {
    // Images
    val singleImage = dialogView.findViewById(R.id.singleDialogIcon) as ImageView

    val doubleLayout = dialogView.findViewById(R.id.doubleDialogIconLayout) as RelativeLayout
    val doubleImage1 = dialogView.findViewById(R.id.doubleDialogIcon1) as ImageView
    val doubleImage2 = dialogView.findViewById(R.id.doubleDialogIcon2) as ImageView

    val tripleLayout = dialogView.findViewById(R.id.tripleDialogIconLayout) as RelativeLayout
    val tripleImage1 = dialogView.findViewById(R.id.tripleDialogIcon1) as ImageView
    val tripleImage2 = dialogView.findViewById(R.id.tripleDialogIcon2) as ImageView
    val tripleImage3 = dialogView.findViewById(R.id.tripleDialogIcon3) as ImageView

    val quadLayout = dialogView.findViewById(R.id.quadDialogIconLayout) as RelativeLayout
    val quadImage1 = dialogView.findViewById(R.id.quadDialogIcon1) as ImageView
    val quadImage2 = dialogView.findViewById(R.id.quadDialogIcon2) as ImageView
    val quadImage3 = dialogView.findViewById(R.id.quadDialogIcon3) as ImageView
    val quadImage4 = dialogView.findViewById(R.id.quadDialogIcon4) as ImageView
    // Content
    val title = dialogView.findViewById(R.id.dialogName) as TextView
    val onlineIcon = dialogView.findViewById(R.id.dialogOnline) as ImageView
    val messageDate = dialogView.findViewById(R.id.dialogDate) as TextView
    val messageImage = dialogView.findViewById(R.id.dialogSenderIcon) as ImageView
    val messageText = dialogView.findViewById(R.id.dialogLastMessage) as TextView
    val unreadBackground = dialogView.findViewById(R.id.unreadBackground) as ImageView
    val unreadMessage = dialogView.findViewById(R.id.unreadMessage) as ImageView
}