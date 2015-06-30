package me.alexeyterekhov.vkfilter.GUI.Common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.rockerhieu.emojicon.EmojiconTextView

class TightTextView : EmojiconTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (View.MeasureSpec.getMode(widthMeasureSpec) != View.MeasureSpec.EXACTLY) {
            val layout = getLayout()
            val lines = layout.getLineCount()
            if (lines > 1) {
                var maxWidth = 0f
                for (i in 0..lines - 1)
                    maxWidth = Math.max(maxWidth, layout.getLineWidth(i))
                val realWidth = Math.round(maxWidth)
                if (realWidth < getMeasuredWidth())
                    super.onMeasure(View.MeasureSpec.makeMeasureSpec(realWidth, View.MeasureSpec.AT_MOST), heightMeasureSpec)
            }
        }
    }
}