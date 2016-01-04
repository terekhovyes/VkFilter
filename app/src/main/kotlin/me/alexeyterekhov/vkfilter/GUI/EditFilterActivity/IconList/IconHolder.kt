package me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.IconList

import android.support.v7.widget.RecyclerView
import android.view.View
import me.alexeyterekhov.vkfilter.GUI.Common.RoundImageView
import me.alexeyterekhov.vkfilter.R


class IconHolder(val v: View): RecyclerView.ViewHolder(v) {
    val icon = (v.findViewById(R.id.iconImage)) as RoundImageView
}