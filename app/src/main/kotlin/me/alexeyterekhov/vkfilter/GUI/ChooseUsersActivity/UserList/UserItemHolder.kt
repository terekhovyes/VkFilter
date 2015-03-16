package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList

import android.view.View
import android.support.v7.widget.RecyclerView
import me.alexeyterekhov.vkfilter.R
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.CheckBox


public class UserItemHolder(v: View): RecyclerView.ViewHolder(v) {
    val singlePic = v.findViewById(R.id.singlePic) as ImageView

    val doubleLayout = v.findViewById(R.id.doublePicLayout) as RelativeLayout
    val doublePic1 = v.findViewById(R.id.doublePic1) as ImageView
    val doublePic2 = v.findViewById(R.id.doublePic2) as ImageView

    val tripleLayout = v.findViewById(R.id.triplePicLayout) as RelativeLayout
    val triplePic1 = v.findViewById(R.id.triplePic1) as ImageView
    val triplePic2 = v.findViewById(R.id.triplePic2) as ImageView
    val triplePic3 = v.findViewById(R.id.triplePic3) as ImageView

    val quadLayout = v.findViewById(R.id.quadPicLayout) as RelativeLayout
    val quadPic1 = v.findViewById(R.id.quadPic1) as ImageView
    val quadPic2 = v.findViewById(R.id.quadPic2) as ImageView
    val quadPic3 = v.findViewById(R.id.quadPic3) as ImageView
    val quadPic4 = v.findViewById(R.id.quadPic4) as ImageView

    val name = v.findViewById(R.id.itemName) as TextView
    val checkBox = v.findViewById(R.id.itemCheckBox) as CheckBox
}