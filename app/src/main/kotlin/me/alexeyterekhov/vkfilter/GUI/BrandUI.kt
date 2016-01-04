package me.alexeyterekhov.vkfilter.GUI

import android.graphics.PorterDuff
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


public object BrandUI {
    public fun brandScrollEffectColors() {
        val context = AppContext.instance
        val glowDrawableId = context.resources.getIdentifier("overscroll_glow", "drawable", "android")
        val edgeDrawableId = context.resources.getIdentifier("overscroll_edge", "drawable", "android")
        val androidGlow = context.resources.getDrawable(glowDrawableId)
        val androidEdge = context.resources.getDrawable(edgeDrawableId)

        var brandColor = context.resources.getColor(R.color.m_black_opacity30)
        brandColor += -16777216

        androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_ATOP)
        androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_ATOP)
    }
}