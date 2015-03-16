package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentManager
import android.support.v4.app.Fragment


public class PagerAdapter(
        val fm: FragmentManager,
        val selectedUsers: MutableSet<Long>,
        val selectedChats: MutableSet<Long>
) : FragmentPagerAdapter(fm) {
    val TITLES = array("Текущие", "Друзья", "Диалоги")

    override fun getCount() = TITLES.size()
    override fun getPageTitle(position: Int) = TITLES.get(position)
    override fun getItem(position: Int): Fragment? {
        return when (position) {
            1 -> {
                val f = FriendChooseFragment()
                f setSelectedUsers selectedUsers
                f
            }
            else -> EmptyFragment()
        }
    }
}