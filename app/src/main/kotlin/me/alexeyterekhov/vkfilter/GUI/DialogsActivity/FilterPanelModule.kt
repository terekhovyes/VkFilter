package me.alexeyterekhov.vkfilter.GUI.DialogsActivity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import io.codetail.animation.SupportAnimator
import io.codetail.animation.ViewAnimationUtils
import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.FilterList.FilterGlassAdapter
import me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.EditFilterActivity
import me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity.ManageFiltersActivity
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestChats
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestUsers
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import java.util.LinkedList

class FilterPanelModule(val activity: DialogsActivity) {
    companion object {
        val SAVED_KEY = "FilterModuleSaved"
        val VISIBLE_KEY = "FilterModuleVisible"
    }

    private val handler = Handler()
    private var blocked = false

    private var manageButtonPressed = false

    fun onCreate(savedState: Bundle?) {
        if (savedState != null) {
            if (savedState containsKey SAVED_KEY)
                if (savedState containsKey VISIBLE_KEY) {
                    with (findMainBtn()) {
                        setVisibility(View.VISIBLE)
                        setImageResource(R.drawable.button_close_noback)
                    }
                    findResetBtn() setVisibility View.VISIBLE
                    findManageBtn() setVisibility View.VISIBLE
                    findLayout() setVisibility View.VISIBLE
                    subscribe()
                }
        }

        findMainBtn() setOnClickListener {
            val list = findList()
            if ((list.getAdapter() as FilterGlassAdapter).filters.isEmpty()) {
                activity.startActivity(Intent(activity, javaClass<EditFilterActivity>()))
            } else {
                when (findLayout().getVisibility()) {
                    View.VISIBLE -> hide()
                    else -> show()
                }
            }
        }

        findManageBtn() setOnClickListener {
            manageButtonPressed = true
            val intent = Intent(AppContext.instance, javaClass<ManageFiltersActivity>())
            activity startActivity intent
        }

        findResetBtn() setOnClickListener {
            val list = findList()
            (list.getAdapter() as FilterGlassAdapter).resetFilters()
        }

        val filtersFromDatabase = DAOFilters.loadVkFilters()

        if (filtersFromDatabase.isEmpty())
            findMainBtn().setVisibility(View.INVISIBLE)
        else
            findMainBtn().setVisibility(View.VISIBLE)

        with (findList()) {
            if (getAdapter() == null) setAdapter(
                    FilterGlassAdapter(
                            this,
                            object : DataDepend {
                                override fun onDataUpdate() {
                                    activity.dialogListModule.getAdapter()!!.checkForFilters()
                                }
                            }
                    )
            )
            if (getLayoutManager() == null) setLayoutManager(LinearLayoutManager(
                    AppContext.instance, LinearLayoutManager.VERTICAL, true
            ))
            val adapter = getAdapter() as FilterGlassAdapter
            adapter.filters.clear()
            adapter.filters addAll filtersFromDatabase
            adapter.notifyDataSetChanged()
        }

        infoRequest(filtersFromDatabase)
    }
    fun onDestroy() {
        unsubscribe()
    }
    fun saveState(): Bundle {
        val bundle = Bundle()
        if (findLayout().getVisibility() == View.VISIBLE) {
            if (manageButtonPressed)
                manageButtonPressed = false
            else {
                bundle.putBoolean(SAVED_KEY, true)
                bundle.putBoolean(VISIBLE_KEY, true)
            }
        }
        return bundle
    }

    private fun subscribe() {
        val adapter = getAdapter()
        if (adapter != null) {
            UserCache.listeners add adapter
            ChatInfoCache.listeners add adapter
        }
    }
    private fun unsubscribe() {
        val adapter = getAdapter()
        if (adapter != null) {
            UserCache.listeners remove adapter
            ChatInfoCache.listeners remove adapter
        }
    }
    private fun infoRequest(filters: List<VkFilter>) {
        val userIds = filters
                .map {
                    it.identifiers()
                            .filter { it.type == VkIdentifier.TYPE_USER }
                            .map { it.id.toString() }
                            .filter { !(UserCache contains it) }}
                .fold(LinkedList<String>(), {
                    res, ids ->
                    res addAll ids
                    res})
                .distinct()
        if (userIds.isNotEmpty())
            RequestControl addBackground RequestUsers(userIds)
        val chatIds = filters
                .map {
                    it.identifiers()
                            .filter { it.type == VkIdentifier.TYPE_CHAT }
                            .map { it.id.toString() }
                            .filter { !(ChatInfoCache contains it) }}
                .fold(LinkedList<String>(), {
                    res, ids ->
                    res addAll ids
                    res})
                .distinct()
        if (chatIds.isNotEmpty())
            RequestControl addBackground RequestChats(chatIds)
    }

    fun getAdapter() = findList().getAdapter() as FilterGlassAdapter?
    private fun findMainBtn() = (activity findViewById R.id.showFilterGlass) as FloatingActionButton
    private fun findResetBtn() = (activity findViewById R.id.filterReset) as FloatingActionButton
    private fun findManageBtn() = (activity findViewById R.id.manageFiltersBtn) as FloatingActionButton
    private fun findLayout() = activity findViewById R.id.glassLayout
    private fun findList() = (activity findViewById R.id.filterList) as RecyclerView

    fun isShown() = findLayout().getVisibility() == View.VISIBLE

    fun show() {
        if (blocked)
            return
        subscribe()
        getAdapter()!!.updateVisibleAvatarLists()
        blocked = true
        val glassLayout = findLayout()
        val btn = findMainBtn()
        val revealDuration = 300L
        findList().scrollToPosition(0)
        with (glassLayout) {
            val animator = ViewAnimationUtils.createCircularReveal(
                    this,
                    btn.getLeft() + btn.getWidth() / 2,
                    btn.getTop() + btn.getHeight() / 2,
                    0f,
                    Math.sqrt((getWidth() * getWidth() + getHeight() * getHeight()).toDouble()).toFloat()
            )
            animator setDuration revealDuration.toInt()
            setVisibility(View.VISIBLE)
            animator.start()
        }
        with (findManageBtn()) {
            val animator = ObjectAnimator.ofFloat(
                    this,
                    "x",
                    btn.getX(),
                    getX()
            )
            animator setDuration revealDuration
            animator setStartDelay revealDuration * 3 / 4
            animator setInterpolator DecelerateInterpolator(1.5f)
            setX(btn.getX())
            setVisibility(View.VISIBLE)
            animator.start()
        }
        with (findResetBtn()) {
            val animator = ObjectAnimator.ofFloat(
                    this,
                    "x",
                    btn.getX(),
                    getX()
            )
            animator setDuration revealDuration
            animator setStartDelay revealDuration
            animator setInterpolator DecelerateInterpolator(1.5f)
            setX(btn.getX())
            setVisibility(View.VISIBLE)
            animator.start()
        }
        btn.setImageResource(R.drawable.button_close_noback)
        val to = 1.3f
        val animation = ScaleAnimation(
                1.0f, to,
                1.0f, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )
        animation.setRepeatMode(Animation.REVERSE)
        animation.setRepeatCount(1)
        animation.setDuration(revealDuration / 3)
        animation.setInterpolator(FastOutSlowInInterpolator())
        btn.startAnimation(animation)
        handler.postDelayed({ blocked = false }, (revealDuration * 2.5).toLong())
    }

    fun hide() {
        if (blocked)
            return
        unsubscribe()
        blocked = true
        val glassLayout = findLayout()
        val btn = findMainBtn()
        val revealDuration = 300L
        with (glassLayout) {
            val animator = ViewAnimationUtils.createCircularReveal(
                    this,
                    btn.getLeft() + btn.getWidth() / 2,
                    btn.getTop() + btn.getHeight() / 2,
                    Math.sqrt((getWidth() * getWidth() + getHeight() * getHeight()).toDouble()).toFloat(),
                    0f
            )
            animator addListener object : SupportAnimator.AnimatorListener {
                override fun onAnimationEnd() {
                    setVisibility(View.INVISIBLE)
                }
                override fun onAnimationStart() {}
                override fun onAnimationCancel() {}
                override fun onAnimationRepeat() {}
            }
            animator setDuration revealDuration.toInt()
            animator.start()
        }
        with (findResetBtn()) {
            val oldX = getX()
            val animator = ObjectAnimator.ofFloat(
                    this,
                    "x",
                    getX(),
                    btn.getX()
            )
            animator setDuration revealDuration / 2
            animator setInterpolator AccelerateInterpolator()
            animator addListener object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    setVisibility(View.INVISIBLE)
                    setX(oldX)
                }
            }
            animator.start()
        }
        with (findManageBtn()) {
            val oldX = getX()
            val animator = ObjectAnimator.ofFloat(
                    this,
                    "x",
                    getX(),
                    btn.getX()
            )
            animator setDuration revealDuration / 2
            animator setStartDelay revealDuration / 4
            animator setInterpolator AccelerateInterpolator()
            animator addListener object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    setVisibility(View.INVISIBLE)
                    setX(oldX)
                }
            }
            animator.start()
        }
        btn.setImageResource(R.drawable.button_filters)
        handler.postDelayed({ blocked = false }, (revealDuration * 1.5).toLong())
    }
}