package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.os.Bundle
import me.alexeyterekhov.vkfilter.R
import com.astuetz.PagerSlidingTabStrip
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarActivity
import java.util.HashSet
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.VkIdentifier


public class ChooseUsersActivity: ActionBarActivity() {
    class object {
        val KEY_FILTER_ID = "ChooseUsersActivityFilterId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_users)

        val filterId = getIntent().getLongExtra(KEY_FILTER_ID, -1)
        val filter = DAOFilters loadVkFilterById filterId
        val ids = filter.identifiers()
        val selectedUsers = HashSet<Long>()
        val selectedChats = HashSet<Long>()
        for (vkId in ids)
            when (vkId.type) {
                VkIdentifier.TYPE_USER -> selectedUsers add vkId.id
                VkIdentifier.TYPE_CHAT -> selectedChats add vkId.id
            }

        val tabs = findViewById(R.id.tabs) as PagerSlidingTabStrip
        tabs.setTextColorResource(R.color.material_greeny_white)
        with (findViewById(R.id.pager) as ViewPager) {
            setAdapter(PagerAdapter(
                    fm = getSupportFragmentManager(),
                    selectedUsers = selectedUsers,
                    selectedChats = selectedChats
            ))
            tabs.setViewPager(this)
        }

        findViewById(R.id.saveFilterButton) setOnClickListener {
            for (vkId in ids)
                vkId.delete()
            for (u in selectedUsers) {
                val vkId = VkIdentifier()
                with (vkId) {
                    id = u
                    type = VkIdentifier.TYPE_USER
                    ownerFilter = filter
                    save()
                }
            }
            for (c in selectedChats) {
                val vkId = VkIdentifier()
                with (vkId) {
                    id = c
                    type = VkIdentifier.TYPE_CHAT
                    ownerFilter = filter
                    save()
                }
            }
            onBackPressed()
        }
    }
}