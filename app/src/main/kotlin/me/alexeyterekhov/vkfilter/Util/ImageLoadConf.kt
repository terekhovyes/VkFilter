package me.alexeyterekhov.vkfilter.Util

import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import me.alexeyterekhov.vkfilter.R


public object ImageLoadConf {
    val loadUser = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.stub_user)
        .showImageOnFail(R.drawable.stub_user)
        .displayer(FadeInBitmapDisplayer(300, true, false, false))
        .resetViewBeforeLoading(false)
        .build()

    val loadUserWithoutAnim = DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.stub_user)
        .showImageOnFail(R.drawable.stub_user)
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