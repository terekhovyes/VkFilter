package me.alexeyterekhov.vkfilter.LibClasses

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

public class EndlessScrollNew(
        val recyclerView: RecyclerView,
        val activationThreshold: Int,
        val reverse: Boolean,
        val onReachThreshold: (currentCount: Int) -> Unit
): RecyclerView.OnScrollListener() {
    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
    var previousTotal = 0
    var loading = true
    var firstVisible = 0
    var visibleCount = 0
    var totalCount = 0

    override fun onScrolled(rv: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        visibleCount = recyclerView.childCount
        totalCount = layoutManager.itemCount
        firstVisible = layoutManager.findFirstVisibleItemPosition()

        if (loading) {
            if (totalCount != previousTotal) {
                loading = false
                previousTotal = totalCount
            }
        }
        if (!loading && (
                !reverse && (firstVisible + visibleCount + activationThreshold >= totalCount)
                || reverse && (firstVisible < activationThreshold))
        ) {
            loading = true
            onReachThreshold(recyclerView.adapter.itemCount)
        }
    }
}