package me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.IconList

import android.support.v7.widget.RecyclerView
import android.view.View
import me.alexeyterekhov.vkfilter.R
import android.view.ViewGroup
import android.view.LayoutInflater
import me.alexeyterekhov.vkfilter.Common.AppContext


class IconAdapter(): RecyclerView.Adapter<IconHolder>() {
    private val icons = createIcons()
    private var selectedIcon: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): IconHolder? {
        val inflater = LayoutInflater.from(AppContext.instance)
        val view = inflater.inflate(R.layout.item_icon, parent, false)
        return IconHolder(view)
    }

    override fun onBindViewHolder(holder: IconHolder, position: Int) {
        with (holder) {
            icon.setImageResource(icons[position])
            chooser setVisibility if (position == selectedIcon) View.VISIBLE else View.INVISIBLE
            v setOnClickListener {
                selectedIcon = position
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = icons.size()

    fun getSelectedIconResource() = icons[selectedIcon]
    fun setSelectedIconResource(res: Int) {
        selectedIcon = icons indexOf res
    }

    private fun createIcons() = arrayListOf(
            R.drawable.group_icon_01,
            R.drawable.group_icon_02,
            R.drawable.group_icon_03,
            R.drawable.group_icon_04,
            R.drawable.group_icon_05
    )
}