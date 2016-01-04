package me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.activeandroid.ActiveAndroid
import com.emtronics.dragsortrecycler.DragSortRecycler
import com.poliveira.parallaxrecycleradapter.ParallaxRecyclerAdapter
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.EditFilterActivity
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DataSaver
import org.lucasr.twowayview.ItemClickSupport
import java.util.*


public class ManageFiltersActivity: VkActivity() {
    companion object {
        val KEY_SAVED = "ManageFiltersSaved"
        val KEY_SELECTION_MODE = "ManageFiltersSelectionMode"
    }

    private var selectionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateOrRestart()
    }
    override fun onRestart() {
        super.onRestart()
        onCreateOrRestart()
    }
    fun onCreateOrRestart() {
        setContentView(R.layout.activity_manage_filters)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val wasSaved = DataSaver.removeObject(KEY_SAVED) != null
        if (wasSaved) {
            selectionMode = (DataSaver removeObject KEY_SELECTION_MODE) as Boolean
        }

        val adapter = FilterAdapter(Vector<VkFilter>())
        val dragSort = object : DragSortRecycler() {
            override fun canDragOver(position: Int) = position != 0
        }
        with (dragSort) {
            setViewHandleId(R.id.iconLayout)
            setOnItemMovedListener({from, to ->
                if (from != 0 && to != 0) {
                    with (adapter.data) {
                        val removed = removeAt(from - 1)
                        add(to - 1, removed)
                    }
                    adapter.notifyDataSetChanged()
                    saveFilterPositions()
                }
            })
        }
        val onItemClick = ItemClickSupport.OnItemClickListener { rv, view, pos, id ->
            if (pos > 0) {
                if (selectionMode) {
                    adapter selectOrDeselect pos
                    if (adapter.nothingSelected())
                        changeMode()
                } else {
                    val filter = adapter.data[pos - 1]
                    val intent = Intent(AppContext.instance, EditFilterActivity::class.java)
                    intent.putExtra(EditFilterActivity.KEY_FILTER_ID, filter.id)
                    startActivity(intent)
                }
            }
        }
        val onItemLongClick = ItemClickSupport.OnItemLongClickListener { rv, view, pos, id ->
            changeMode()
            if (selectionMode) {
                adapter select pos
            } else {
                adapter.deselectAll()
            }
            true
        }
        with (findViewById(R.id.filterList) as RecyclerView) {
            setAdapter(adapter)
            layoutManager = LinearLayoutManager(AppContext.instance)
            itemAnimator = animator()
            addItemDecoration(dragSort)
            addOnItemTouchListener(dragSort)
            setOnScrollListener(dragSort.scrollListener)
            val clickAdapter = ItemClickSupport.addTo(this)
            clickAdapter.setOnItemClickListener(onItemClick)
            clickAdapter.setOnItemLongClickListener(onItemLongClick)

            val inflater = LayoutInflater.from(this@ManageFiltersActivity)
            val view = inflater.inflate(R.layout.activity_manage_filter_header, this, false)
            adapter.setParallaxHeader(view, this)
        }
        with (findViewById(R.id.manageFilterButton) as FloatingActionButton) {
            setOnClickListener {
                if (selectionMode) {
                    val handler = Handler()
                    val temporaryRemoved = adapter.removeSelected()
                    val deleteAction = Runnable { deleteFilters(temporaryRemoved) }
                    handler.postDelayed(deleteAction, 3500)
                    val title = getString(R.string.manage_filters_deleted) + " ${temporaryRemoved.size}"
                    Snackbar
                            .make(this, title, Snackbar.LENGTH_LONG)
                            .setAction(R.string.manage_filters_cancel, { view ->
                                handler.removeCallbacks(deleteAction)
                                addFiltersToAdapter(temporaryRemoved)
                            })
                            .show()

                    changeMode()
                } else {
                    startActivity(Intent(AppContext.instance, EditFilterActivity::class.java))
                }
            }
            setImageResource(
                    if (selectionMode)
                        R.drawable.button_delete
                    else
                        R.drawable.button_add
            )
            val list = (this@ManageFiltersActivity).findViewById(R.id.filterList) as RecyclerView
            adapter.setOnParallaxScroll(object : ParallaxRecyclerAdapter.OnParallaxScroll {
                val EDGE = 0.6f
                var top = y
                val interpolator = AccelerateDecelerateInterpolator()

                private fun scrollPos(percent: Float): Float {
                    return top * (1f - percent) - height / 2 * percent
                }
                private fun translatePos(percent: Float): Float {
                    val from = scrollPos(EDGE)
                    val to = list.height - height - height / 16 * 5
                    val translatePercent = interpolator.getInterpolation((percent - EDGE) / (1f - EDGE))
                    return from + (to - from) * translatePercent
                }
                override fun onParallaxScroll(percent: Float, p1: Float, p2: View?) {
                    if (top == 0f)
                        top = y
                    y = if (percent < EDGE) scrollPos(percent) else translatePos(percent)
                }
            })
        }
    }
    override fun onResume() {
        super.onResume()
        refreshList()
    }
    override fun onPause() {
        super.onPause()
        saveFilterPositions()
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        with (DataSaver) {
            putObject(KEY_SAVED, true)
            putObject(KEY_SELECTION_MODE, selectionMode)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshList() {
        val adapter = getAdapter()
        adapter.data.clear()
        adapter.data.addAll(DAOFilters.loadVkFilters())
        adapter.notifyDataSetChanged()
    }
    private fun changeMode() {
        selectionMode = !selectionMode
        (findViewById(R.id.manageFilterButton) as FloatingActionButton).setImageResource(
                if (selectionMode)
                    R.drawable.button_delete
                else
                    R.drawable.button_add
        )

        val button = findViewById(R.id.manageFilterButton) as FloatingActionButton
        val to = 1.3f
        val animation = ScaleAnimation(
                1.0f, to,
                1.0f, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )
        animation.repeatMode = Animation.REVERSE
        animation.repeatCount = 1
        animation.duration = 100
        animation.interpolator = FastOutSlowInInterpolator()
        button.startAnimation(animation)
    }
    private fun addFiltersToAdapter(c: Collection<VkFilter>) {
        val adapter = getAdapter()
        if (adapter.data.isEmpty()) {
            adapter.data.addAll(c)
            adapter.notifyDataSetChanged()
        } else {
            for (f in c) {
                var pos = 0
                for (i in 0..adapter.data.size - 1) {
                    if ((adapter.data[i]).listOrder > f.listOrder) {
                        pos = i
                        break
                    }
                }
                adapter.data.add(pos, f)
                adapter.notifyItemInserted(pos)
            }
        }
    }
    private fun deleteFilters(c: Collection<VkFilter>) {
        for (filter in c) {
            for (id in filter.identifiers())
                id.delete()
            DAOFilters.deleteFilter(filter)
        }
    }
    private fun saveFilterPositions() {
        val adapter = getAdapter()
        ActiveAndroid.beginTransaction()
        try {
            for (i in 0..adapter.data.size - 1) {
                val f = adapter.data[i]
                f.listOrder = i
                DAOFilters.saveFilter(f)
            }
            ActiveAndroid.setTransactionSuccessful()
        } finally {
            ActiveAndroid.endTransaction()
        }
    }
    private fun getAdapter() = (findViewById(R.id.filterList) as RecyclerView).adapter as FilterAdapter
    private fun animator() = object : DefaultItemAnimator() {
        init {
            supportsChangeAnimations = true
            changeDuration = 150
        }
    }
}