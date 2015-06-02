package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.HashSet


public class PagerAdapter(
        val fm: FragmentManager,
        val selectedUsers: MutableSet<Long>,
        val selectedChats: MutableSet<Long>
) : FragmentPagerAdapter(fm) {
    val TITLES = arrayOf("Текущие", "Друзья", "Диалоги")

    private val immutableUsers = HashSet<Long>(selectedUsers)
    private val immutableChats = HashSet<Long>(selectedChats)

    private var friendFragment: FriendChooseFragment? = null
    private var dialogFragment: DialogChooseFragment? = null
    private var currentFragment: CurrentChooseFragment? = null

    override fun getCount() = TITLES.size()
    override fun getPageTitle(position: Int) = TITLES.get(position)
    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> {
                if (currentFragment == null)
                    currentFragment = CurrentChooseFragment()
                currentFragment!!.setSelected(
                        immutableUsers,
                        immutableChats,
                        selectedUsers,
                        selectedChats,
                        {
                            dialogFragment?.adapter?.notifyDataSetChanged()
                            friendFragment?.adapter?.notifyDataSetChanged()
                        }
                )
                currentFragment
            }
            1 -> {
                if (friendFragment == null)
                    friendFragment = FriendChooseFragment()
                friendFragment!!.setSelectedUsers(
                        selectedUsers,
                        {
                            dialogFragment?.adapter?.notifyDataSetChanged()
                            currentFragment?.adapter?.notifyDataSetChanged()
                        }
                )
                friendFragment
            }
            2 -> {
                if (dialogFragment == null)
                    dialogFragment = DialogChooseFragment()
                dialogFragment!!.setSelected(
                        selectedUsers,
                        selectedChats,
                        {
                            friendFragment?.adapter?.notifyDataSetChanged()
                            currentFragment?.adapter?.notifyDataSetChanged()
                        }
                )
                dialogFragment
            }
            else -> EmptyFragment()
        }
    }
}