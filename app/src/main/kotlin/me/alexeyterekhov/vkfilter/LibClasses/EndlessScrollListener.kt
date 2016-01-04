package me.alexeyterekhov.vkfilter.LibClasses

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView


public abstract class EndlessScrollListener(
        val recyclerView: RecyclerView,
        val activationThreshold: Int
): RecyclerView.OnScrollListener() {
    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
    var previousTotal = 0
    var loading = true
    var firstVisible = 0
    var visibleCount = 0
    var totalCount = 0

    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(rv, dx, dy)
        visibleCount = recyclerView.childCount
        totalCount = layoutManager.itemCount
        firstVisible = layoutManager.findFirstVisibleItemPosition()

        if (loading) {
            if (totalCount != previousTotal) {
                loading = false
                previousTotal = totalCount
            }
        }
        if (!loading && (firstVisible
                + visibleCount
                + activationThreshold
                >= totalCount)) {
            loading = true
            onReachThreshold(recyclerView.adapter.itemCount)
        }
    }

    abstract fun onReachThreshold(currentItemCount: Int)
}