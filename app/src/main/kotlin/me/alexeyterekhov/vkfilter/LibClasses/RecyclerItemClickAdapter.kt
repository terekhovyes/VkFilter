package me.alexeyterekhov.vkfilter.LibClasses

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import me.alexeyterekhov.vkfilter.LibClasses.RecyclerItemClickAdapter.OnItemClickListener

public class RecyclerItemClickAdapter(
        val context: Context,
        val clickListener: OnItemClickListener
): RecyclerView.OnItemTouchListener {
    public trait OnItemClickListener {
        public fun onItemClick(v: View, pos: Int)
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val childView = rv.findChildViewUnder(e.getX(), e.getY())
        if (childView != null && gestureDetector.onTouchEvent(e)) {
            clickListener.onItemClick(childView, rv.getChildPosition(childView))
            return true
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {}

    private val gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent?) = true
            })
}