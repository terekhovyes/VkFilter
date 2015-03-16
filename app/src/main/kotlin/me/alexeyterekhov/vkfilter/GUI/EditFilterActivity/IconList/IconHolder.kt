package me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.IconList

import android.view.View
import android.support.v7.widget.RecyclerView
import me.alexeyterekhov.vkfilter.R
import android.widget.ImageView


class IconHolder(val v: View): RecyclerView.ViewHolder(v) {
    val chooser = v findViewById R.id.chooserView
    val icon = (v findViewById R.id.iconImage) as ImageView
}