package me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import me.alexeyterekhov.vkfilter.R


class FilterItemHolder(val item: View): RecyclerView.ViewHolder(item) {
    val icon = (item findViewById R.id.filterIcon) as ImageView
    val name = (item findViewById R.id.filterName) as TextView
    val selection = (item findViewById R.id.selectionBackground) as ImageView
    val avatarList = (item findViewById R.id.avatarList) as RecyclerView
}