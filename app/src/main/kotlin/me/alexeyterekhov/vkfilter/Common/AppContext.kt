package me.alexeyterekhov.vkfilter.Common

import android.content.Context
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import me.alexeyterekhov.vkfilter.R
import kotlin.properties.Delegates

public object AppContext {
    public var instance: Context by Delegates.notNull()
}

public class MyApp : com.activeandroid.app.Application() {
    init {
        AppContext.instance = this
    }

    override fun onCreate() {
        super.onCreate()
        // UniversalImageLoader
        val defaultDisplayOptions = DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(R.drawable.user_photo_loading)
                .showImageOnFail(R.drawable.user_photo_loading)
                .displayer(FadeInBitmapDisplayer(300, true, false, false))
                .resetViewBeforeLoading(false)
                .build()
        val libConfig = ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultDisplayOptions)
                .diskCacheSize(31457280)
                .build()
        ImageLoader.getInstance().init(libConfig)
    }
}