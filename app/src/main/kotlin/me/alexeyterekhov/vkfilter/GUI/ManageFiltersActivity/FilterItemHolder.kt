package me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity

import android.view.View
import android.support.v7.widget.RecyclerView
import me.alexeyterekhov.vkfilter.R
import android.widget.TextView
import android.widget.ImageView


class FilterItemHolder(val item: View): RecyclerView.ViewHolder(item) {
    val icon = (item findViewById R.id.filterIcon) as ImageView
    val name = (item findViewById R.id.filterName) as TextView
    val selection = (item findViewById R.id.selectionBackground) as ImageView
    val avatarList = (item findViewById R.id.avatarList) as RecyclerView
}