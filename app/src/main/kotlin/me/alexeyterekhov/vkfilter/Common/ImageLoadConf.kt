package me.alexeyterekhov.vkfilter.Common

import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import me.alexeyterekhov.vkfilter.R


public object ImageLoadConf {
    val loadUser = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.user_photo_loading)
        .showImageOnFail(R.drawable.user_photo_loading)
        .displayer(FadeInBitmapDisplayer(300, true, false, false))
        .resetViewBeforeLoading(false)
        .build()

    val loadUserWithoutAnim = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.user_photo_loading)
        .showImageOnFail(R.drawable.user_photo_loading)
        .resetViewBeforeLoading(false)
        .build()

    val loadImage = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.image_loading)
        .showImageOnFail(R.drawable.image_loading)
        .displayer(FadeInBitmapDisplayer(300, true, false, false))
        .resetViewBeforeLoading(false)
        .build()

    val loadImageWithoutAnim = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.image_loading)
        .showImageOnFail(R.drawable.image_loading)
        .resetViewBeforeLoading(false)
        .build()
}