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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.ChooseUsersActivity
import me.alexeyterekhov.vkfilter.GUI.Common.AvatarList.AvatarAdapter
import me.alexeyterekhov.vkfilter.GUI.Common.TripleSwitchView
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.IconList.IconAdapter
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DataSaver
import me.alexeyterekhov.vkfilter.Util.FilterStates
import java.util.LinkedList
import kotlin.properties.Delegates

public class EditFilterActivity : VkActivity() {
    companion object {
        val KEY_FILTER_ID = "filter_id"
        // Saver
        val KEY_SAVED_FILTER = "EditFilter_Filter"
        val KEY_SAVED_IDS_BACKUP = "EditFilter_IdsBackup"
    }

    private var filter: VkFilter by Delegates.notNull()
    private var filterState = 0
    private var idsBackup = LinkedList<VkIdentifier>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_filter)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        restoreData()

        findFilterName() setText filter.filterName

        with (findIconList()) {
            if (getAdapter() == null) {
                setLayoutManager(LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, false))
                val adapter = IconAdapter()
                adapter setSelectedIconId filter.getIcon()
                setAdapter(adapter)
            }
        }

        findStateLabel() setText FilterStates.filterToString(filter.state)
        with (findStateSwitch()) {
            setListener(object : TripleSwitchView.OnSwitchChangeStateListener {
                override fun onChangeState(newState: Int) {
                    findStateLabel() setText FilterStates.switchToString(newState)
                    filterState = FilterStates.switchToFilter(newState)
                }
            })
            setStateWithListener(FilterStates.filterToSwitch(filter.state), false)
        }

        val actionOpenChooseActivity = {
            val intent = Intent(AppContext.instance, javaClass<ChooseUsersActivity>())
            intent.putExtra(
                    ChooseUsersActivity.KEY_FILTER_ID,
                    filter.getId()
            )
            startActivity(intent)
        }
        findPeopleEmptyView() setOnClickListener { actionOpenChooseActivity() }
        with (findPeopleList()) {
            if (getAdapter() == null) {
                setLayoutManager(LinearLayoutManager(AppContext.instance, LinearLayoutManager.HORIZONTAL, false))
                val adapter = AvatarAdapter(R.layout.item_avatar_50dp)
                filter.invalidateCache()
                adapter setIds filter.identifiers()
                setAdapter(adapter)
                val gestureDetector = GestureDetector(
                        AppContext.instance,
                        object : GestureDetector.SimpleOnGestureListener() {
                            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                                actionOpenChooseActivity()
                                return true
                            }
                        }
                )
                setOnTouchListener { view, motionEvent ->
                    gestureDetector.onTouchEvent(motionEvent)
                }
            }
        }

        val filterJustCreated = getFilterId() == -1L
        with (findCancelButton()) {
            setAlpha(if (filterJustCreated) 0.2f else 1f)
            if (!filterJustCreated) {
                setOnClickListener {
                    filter.invalidateCache()
                    filter.identifiers() forEach { it.delete() }
                    idsBackup forEach {
                        val vk = VkIdentifier()
                        with (vk) {
                            id = it.id
                            type = it.type
                            ownerFilter = filter
                            save()
                        }
                    }
                    idsBackup.clear()
                    filter.invalidateCache()
                    super.onBackPressed()
                }
            }
        }
        findDeleteButton() setOnClickListener {
            DAOFilters.deleteFilter(filter)
            super.onBackPressed()
        }
        findSaveButton() setOnClickListener {
            Toast.makeText(this, R.string.a_edit_filter_toast_save, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter = getPeopleAdapter()
        filter.invalidateCache()
        adapter.setIds(filter.identifiers())
        findPeopleEmptyView() setVisibility if (adapter.getItemCount() == 0)
            View.VISIBLE
        else
            View.GONE
        UserCache.listeners add adapter
    }

    override fun onPause() {
        super.onPause()
        UserCache.listeners remove getPeopleAdapter()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        fillFilter(filter)
        with (DataSaver) {
            putObject(KEY_SAVED_FILTER, filter)
            putObject(KEY_SAVED_IDS_BACKUP, LinkedList(idsBackup))
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

    private fun findFilterName() = findViewById(R.id.filterName) as EditText
    private fun findIconList() = findViewById(R.id.iconList) as RecyclerView
    private fun findStateLabel() = findViewById(R.id.filterStateLabel) as TextView
    private fun findStateSwitch() = findViewById(R.id.filterStateSwitch) as TripleSwitchView
    private fun findPeopleEmptyView() = findViewById(R.id.peopleEmptyView)
    private fun findPeopleList() = findViewById(R.id.peopleList) as RecyclerView
    private fun findCancelButton() = findViewById(R.id.cancelButton) as Button
    private fun findDeleteButton() = findViewById(R.id.deleteButton) as Button
    private fun findSaveButton() = findViewById(R.id.saveButton) as Button
    private fun getIconAdapter() = findIconList().getAdapter() as IconAdapter
    private fun getPeopleAdapter() = findPeopleList().getAdapter() as AvatarAdapter

    private fun restoreData() {
        filter = restoreOrCreateFilter(DataSaver contains KEY_SAVED_FILTER)
        filterState = filter.state
        idsBackup.clear()
        idsBackup addAll if (DataSaver contains KEY_SAVED_IDS_BACKUP)
            (DataSaver removeObject KEY_SAVED_IDS_BACKUP) as LinkedList<VkIdentifier>
        else
            filter.identifiers()
    }

    private fun restoreOrCreateFilter(fromSaver: Boolean): VkFilter {
        return when {
            fromSaver && DataSaver contains KEY_SAVED_FILTER -> {
                (DataSaver removeObject KEY_SAVED_FILTER) as VkFilter
            }
            getFilterId() != -1L -> DAOFilters loadVkFilterById getFilterId()
            else -> createVkFilter()
        }
    }

    private fun getFilterId() = getIntent().getLongExtra(KEY_FILTER_ID, -1L)

    private fun createVkFilter(): VkFilter {
        val filter = VkFilter()
        DAOFilters.saveFilter(filter)
        return filter
    }

    private fun fillFilter(f: VkFilter) {
        val enteredName = findFilterName().getText().toString()
        with (f) {
            setIcon(getIconAdapter().getSelectedIconId())
            filterName = enteredName
            state = this@EditFilterActivity.filterState
        }
    }

    private fun saveFilter(f: VkFilter) = DAOFilters.saveFilter(f)
}