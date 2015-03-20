package me.alexeyterekhov.vkfilter.GUI.Common.AvatarList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import me.alexeyterekhov.vkfilter.R

class AvatarHolder(val v: View): RecyclerView.ViewHolder(v) {
    val singleImage = v.findViewById(R.id.singleDialogIcon) as ImageView

    val doubleLayout = v.findViewById(R.id.doubleDialogIconLayout) as RelativeLayout
    val doubleImage1 = v.findViewById(R.id.doubleDialogIcon1) as ImageView
    val doubleImage2 = v.findViewById(R.id.doubleDialogIcon2) as ImageView

    val tripleLayout = v.findViewById(R.id.tripleDialogIconLayout) as RelativeLayout
    val tripleImage1 = v.findViewById(R.id.tripleDialogIcon1) as ImageView
    val tripleImage2 = v.findViewById(R.id.tripleDialogIcon2) as ImageView
    val tripleImage3 = v.findViewById(R.id.tripleDialogIcon3) as ImageView

    val quadLayout = v.findViewById(R.id.quadDialogIconLayout) as RelativeLayout
    val quadImage1 = v.findViewById(R.id.quadDialogIcon1) as ImageView
    val quadImage2 = v.findViewById(R.id.quadDialogIcon2) as ImageView
    val quadImage3 = v.findViewById(R.id.quadDialogIcon3) as ImageView
    val quadImage4 = v.findViewById(R.id.quadDialogIcon4) as ImageView
}