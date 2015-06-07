package me.alexeyterekhov.vkfilter.GUI.Common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout


class SwipeOpener : FrameLayout {
    private val DOWN_ZONE = 0.10
    private val UP_ZONE = 0.20
    private var swipeStarted = false
    private var listener: OpenerListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> if (getWidth() * DOWN_ZONE > event.getX()) {
                swipeStarted = true
                return true
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> if (swipeStarted) {
                if (getWidth() * UP_ZONE < event.getX()) {
                    swipeStarted = false
                    listener?.onOpen()
                }
                return true
            }
        }
        super.onTouchEvent(event)
        return false
    }

    fun setListener(l: OpenerListener?) { listener = l }

    interface OpenerListener {
        fun onOpen()
    }
}