package me.alexeyterekhov.vkfilter.Util

import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import me.alexeyterekhov.vkfilter.R


public object ImageLoadConf {
    val loadUser = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.icon_user_stub)
        .showImageOnFail(R.drawable.icon_user_stub)
        .displayer(FadeInBitmapDisplayer(300, true, false, false))
        .resetViewBeforeLoading(false)
        .build()

    val loadUserWithoutAnim = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.icon_user_stub)
        .showImageOnFail(R.drawable.icon_user_stub)
        .resetViewBeforeLoading(false)
        .build()

    val loadImage = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.icon_image_stub)
        .showImageOnFail(R.drawable.icon_image_stub)
        .displayer(FadeInBitmapDisplayer(300, true, false, false))
        .resetViewBeforeLoading(false)
        .build()

    val loadImageWithoutAnim = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.icon_image_stub)
        .showImageOnFail(R.drawable.icon_image_stub)
        .resetViewBeforeLoading(false)
        .build()
}