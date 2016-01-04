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
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (width * DOWN_ZONE > event.x) {
                swipeStarted = true
                return true
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> if (swipeStarted) {
                if (width * UP_ZONE < event.x) {
                    swipeStarted = false
                    listener?.onOpen()
                }
                return true
            }
        }
        super.onTouchEvent(event)
        return false
    }

    infix fun setListener(l: OpenerListener?) { listener = l }

    interface OpenerListener {
        fun onOpen()
    }
}