package me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.IconList

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import me.alexeyterekhov.vkfilter.R


class IconHolder(val v: View): RecyclerView.ViewHolder(v) {
    val chooser = v findViewById R.id.chooserView
    val icon = (v findViewById R.id.iconImage) as ImageView
}