package me.alexeyterekhov.vkfilter.GUI.EditFilterActivity

import android.support.v7.app.ActionBarActivity
import android.os.Bundle
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Database.VkFilter
import android.widget.EditText
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.Common.DataSaver
import kotlin.properties.Delegates
import me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.IconList.IconAdapter
import android.support.v7.widget.GridLayoutManager
import me.alexeyterekhov.vkfilter.GUI.Common.AvatarList.AvatarAdapter
import android.content.Intent
import me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.ChooseUsersActivity
import android.view.GestureDetector
import android.view.MotionEvent
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.GUI.Common.TripleSwitchView
import android.widget.TextView
import me.alexeyterekhov.vkfilter.Common.FilterStates


public class EditFilterActivity: ActionBarActivity() {
    class object {
        // Intent
        val KEY_FILTER_ID = "filter_id"
        // Saver
        val KEY_SAVED = "EditFilterActivitySaved"
        val KEY_ICON_ADAPTER = "EditFilterActivityIconAdapter"
        val KEY_AVATAR_ADAPTER = "EditFilterActivityAvatarAdapter"
        val KEY_FILTER = "EditFilterActivityCurrentFilter"
    }

    private var filter: VkFilter by Delegates.notNull()
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
        val intent = getIntent()
        val wasSaved = (DataSaver removeObject KEY_SAVED) != null

        filter = when {
            wasSaved && DataSaver contains KEY_FILTER -> {
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
        selectedState = filter.state

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

        with (findViewById(R.id.memberList) as RecyclerView) {
            setLayoutManager(GridLayoutManager(AppContext.instance, 4))
            val adapter = if (wasSaved)
                              (DataSaver removeObject KEY_AVATAR_ADAPTER) as AvatarAdapter
                          else
                              AvatarAdapter(R.layout.item_avatar_70dp)
            adapter setIds filter.identifiers()
            setAdapter(adapter)
            val gestureDetector = GestureDetector(
                    AppContext.instance,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: MotionEvent?) = true
                    })
            setOnTouchListener {
                (view, motionEvent) ->
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    val startIntent = Intent(AppContext.instance, javaClass<ChooseUsersActivity>())
                    startIntent.putExtra(
                            ChooseUsersActivity.KEY_FILTER_ID,
                            filter.getId()
                    )
                    startActivity(startIntent)
                    true
                } else
                    false
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        with (DataSaver) {
            putObject(KEY_SAVED, true)
            putObject(KEY_ICON_ADAPTER, getIconAdapter())
            putObject(KEY_AVATAR_ADAPTER, getAvatarAdapter())
            putObject(KEY_FILTER, filter)
        }
    }
    override fun onResume() {
        super.onResume()
        UserCache.listeners add getAvatarAdapter()
    }
    override fun onPause() {
        super.onPause()
        UserCache.listeners remove getAvatarAdapter()
        val enteredName = (findViewById(R.id.filterName) as EditText).getText().toString()
        with (filter) {
            setIconResource(getIconAdapter().getSelectedIconResource())
            filterName = enteredName
            state = selectedState
            DAOFilters.saveFilter(this)
        }
    }

    private fun createVkFilter(): VkFilter {
        val filter = VkFilter()
        DAOFilters.saveFilter(filter)
        return filter
    }

    private fun getIconAdapter() = (findViewById(R.id.iconList) as RecyclerView)
            .getAdapter() as IconAdapter
    private fun getAvatarAdapter() = (findViewById(R.id.memberList) as RecyclerView)
            .getAdapter() as AvatarAdapter
}