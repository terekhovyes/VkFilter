package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import me.alexeyterekhov.vkfilter.R


public class DialogItemHolder(v: View): RecyclerView.ViewHolder(v) {
    val singlePic = v.findViewById(R.id.singleDialogIcon) as ImageView

    val doubleLayout = v.findViewById(R.id.doubleDialogIconLayout) as RelativeLayout
    val doublePic1 = v.findViewById(R.id.doubleDialogIcon1) as ImageView
    val doublePic2 = v.findViewById(R.id.doubleDialogIcon2) as ImageView

    val tripleLayout = v.findViewById(R.id.tripleDialogIconLayout) as RelativeLayout
    val triplePic1 = v.findViewById(R.id.tripleDialogIcon1) as ImageView
    val triplePic2 = v.findViewById(R.id.tripleDialogIcon2) as ImageView
    val triplePic3 = v.findViewById(R.id.tripleDialogIcon3) as ImageView

    val quadLayout = v.findViewById(R.id.quadDialogIconLayout) as RelativeLayout
    val quadPic1 = v.findViewById(R.id.quadDialogIcon1) as ImageView
    val quadPic2 = v.findViewById(R.id.quadDialogIcon2) as ImageView
    val quadPic3 = v.findViewById(R.id.quadDialogIcon3) as ImageView
    val quadPic4 = v.findViewById(R.id.quadDialogIcon4) as ImageView

    val name = v.findViewById(R.id.dialogName) as TextView
    val senderIcon = v.findViewById(R.id.dialogSenderIcon) as ImageView
    val lastMessage = v.findViewById(R.id.dialogLastMessage) as TextView
    val checkBox = v.findViewById(R.id.itemCheckBox) as CheckBox
}