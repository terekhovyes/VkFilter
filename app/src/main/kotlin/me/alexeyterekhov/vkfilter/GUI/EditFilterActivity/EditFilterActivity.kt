package me.alexeyterekhov.vkfilter.GUI.EditFilterActivity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.ChooseUsersActivity
import me.alexeyterekhov.vkfilter.GUI.Common.AvatarList.AvatarListAdapter
import me.alexeyterekhov.vkfilter.GUI.Common.TripleSwitchView
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.IconList.IconAdapter
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DataSaver
import me.alexeyterekhov.vkfilter.Util.FilterStates
import java.util.LinkedList
import kotlin.properties.Delegates


public class EditFilterActivity: VkActivity() {
    companion object {
        // Intent
        val KEY_FILTER_ID = "filter_id"
        // Saver
        val KEY_SAVED = "EditFilterActivitySaved"
        val KEY_ICON_ADAPTER = "EditFilterActivityIconAdapter"
        val KEY_AVATAR_ADAPTER = "EditFilterActivityAvatarAdapter"
        val KEY_FILTER = "EditFilterActivityCurrentFilter"
        val KEY_FILTER_IDS = "EditFilterActivityFilterIds"
    }

    private var filter: VkFilter by Delegates.notNull()
    private var oldIdentifiers = LinkedList<VkIdentifier>()
    private var selectedState = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateOrRestart()
    }
    override fun onRestart() {
        super.onRestart()
        onCreateOrRestart()
    }
    fun onCreateOrRestart() {
        setContentView(R.layout.activity_edit_filter)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        val intent = getIntent()
        val wasSaved = (DataSaver removeObject KEY_SAVED) != null

        filter = loadOrCreateFilter(intent, wasSaved)
        selectedState = filter.state

        oldIdentifiers.clear()
        if (wasSaved) {
            val saved = (DataSaver removeObject KEY_FILTER_ID) as LinkedList<VkIdentifier>
            oldIdentifiers addAll saved
        } else
            oldIdentifiers addAll filter.identifiers()

        findViewById(R.id.filterName) as EditText setText filter.filterName

        with (findViewById(R.id.iconList) as RecyclerView) {
            setLayoutManager(LinearLayoutManager(
                    AppContext.instance,
                    LinearLayoutManager.HORIZONTAL,
                    false
            ))
            setAdapter(
                    if (wasSaved)
                        (DataSaver removeObject KEY_ICON_ADAPTER) as IconAdapter
                    else {
                        val a = IconAdapter()
                        a setSelectedIconResource filter.getIconResource()
                        a
                    }
            )
        }

        val stateView = findViewById(R.id.filterStateLabel) as TextView
        stateView setText FilterStates.filterToString(filter.state)
        with (findViewById(R.id.filterStateSwitch) as TripleSwitchView) {
            setListener(object : TripleSwitchView.OnSwitchChangeStateListener {
                override fun onChangeState(newState: Int) {
                    stateView setText FilterStates.switchToString(newState)
                    selectedState = FilterStates.switchToFilter(newState)
                }
            })
            setStateWithListener(FilterStates.filterToSwitch(filter.state), false)
        }

        // Member list block
        val openChooseActivity = {
            val startIntent = Intent(AppContext.instance, javaClass<ChooseUsersActivity>())
            startIntent.putExtra(
                    ChooseUsersActivity.KEY_FILTER_ID,
                    filter.getId()
            )
            startActivity(startIntent)
        }
        val emptyView = findViewById(R.id.membersEmptyView)
        emptyView setOnClickListener { openChooseActivity() }
        with (findViewById(R.id.memberList) as GridView) {
            val adapter = if (wasSaved)
                              (DataSaver removeObject KEY_AVATAR_ADAPTER) as AvatarListAdapter
                          else
                              AvatarListAdapter(R.layout.item_avatar_70dp)
            filter.invalidateCache()
            adapter setIds filter.identifiers()
            setAdapter(adapter)
            val gestureDetector = GestureDetector(
                AppContext.instance,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent?) = true
                })
            setOnTouchListener {
                view, motionEvent ->
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    openChooseActivity()
                    true
                } else
                    false
            }
            setEmptyView(emptyView)
        }

        val filterJustCreated = !intent.hasExtra(KEY_FILTER_ID) || intent.getLongExtra(KEY_FILTER_ID, -1) == -1L
        with (findViewById(R.id.cancelButton)) {
            setVisibility(if (filterJustCreated) View.GONE else View.VISIBLE)
            setOnClickListener {
                filter.invalidateCache()
                filter.identifiers() forEach { it.delete() }
                oldIdentifiers forEach {
                    val vk = VkIdentifier()
                    with (vk) {
                        id = it.id
                        type = it.type
                        ownerFilter = filter
                        save()
                    }
                }
                oldIdentifiers.clear()
                filter.invalidateCache()
                super.onBackPressed()
            }
        }
        findViewById(R.id.deleteButton) setOnClickListener {
            DAOFilters.deleteFilter(filter)
            super.onBackPressed()
        }
    }
    override fun onResume() {
        super.onResume()
        UserCache.listeners add getAvatarAdapter()
    }
    override fun onPause() {
        super.onPause()
        UserCache.listeners remove getAvatarAdapter()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        fillFilter(filter)
        with (DataSaver) {
            putObject(KEY_SAVED, true)
            putObject(KEY_ICON_ADAPTER, getIconAdapter())
            putObject(KEY_AVATAR_ADAPTER, getAvatarAdapter())
            putObject(KEY_FILTER, filter)
            val copy = LinkedList(oldIdentifiers)
            putObject(KEY_FILTER_ID, copy)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        fillFilter(filter)
        saveFilter(filter)
        super.onBackPressed()
    }

    private fun createVkFilter(): VkFilter {
        val filter = VkFilter()
        DAOFilters.saveFilter(filter)
        return filter
    }

    private fun fillFilter(f: VkFilter) {
        val enteredName = (findViewById(R.id.filterName) as EditText).getText().toString()
        with (f) {
            setIconResource(getIconAdapter().getSelectedIconResource())
            filterName = enteredName
            state = selectedState
        }
    }

    private fun saveFilter(f: VkFilter) {
        DAOFilters.saveFilter(f)
    }

    private fun loadOrCreateFilter(intent: Intent, useSaver: Boolean): VkFilter {
        return when {
            useSaver && DataSaver contains KEY_FILTER -> {
                (DataSaver removeObject KEY_FILTER) as VkFilter
            }
            intent.hasExtra(KEY_FILTER_ID) -> {
                val id = intent.getLongExtra(KEY_FILTER_ID, -1)
                if (id != -1L)
                    DAOFilters loadVkFilterById id
                else
                    createVkFilter()
            }
            else -> createVkFilter()
        }
    }

    private fun getIconAdapter() = (findViewById(R.id.iconList) as RecyclerView)
            .getAdapter() as IconAdapter
    private fun getAvatarAdapter() = (findViewById(R.id.memberList) as GridView)
            .getAdapter() as AvatarListAdapter
}