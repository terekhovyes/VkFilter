package me.alexeyterekhov.vkfilter.GUI

import android.graphics.PorterDuff
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.R


public object BrandUI {
    public fun brandScrollEffectColors() {
        val context = AppContext.instance
        val glowDrawableId = context.getResources().getIdentifier("overscroll_glow", "drawable", "android")
        val edgeDrawableId = context.getResources().getIdentifier("overscroll_edge", "drawable", "android")
        val androidGlow = context.getResources().getDrawable(glowDrawableId)
        val androidEdge = context.getResources().getDrawable(edgeDrawableId)

        var brandColor = context.getResources().getColor(R.color.main_dark_white)
        brandColor += -16777216

        androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_ATOP)
        androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_ATOP)
    }
}