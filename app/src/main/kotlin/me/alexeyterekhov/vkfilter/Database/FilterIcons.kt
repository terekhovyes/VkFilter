package me.alexeyterekhov.vkfilter.Database

import me.alexeyterekhov.vkfilter.R


public object FilterIcons {
    private val filterIcons = hashMapOf(
            1 to R.drawable.group_icon_01,
            2 to R.drawable.group_icon_02,
            3 to R.drawable.group_icon_03,
            4 to R.drawable.group_icon_04,
            5 to R.drawable.group_icon_05
    )

    fun count() = filterIcons.size
    fun resourceById(id: Int): Int {
        return if (filterIcons.containsKey(id))
            filterIcons[id]!!
        else
            R.drawable.group_icon_01
    }
}