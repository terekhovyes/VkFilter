package me.alexeyterekhov.vkfilter.Common

import android.content.Context
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
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
        val libConfig = ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(ImageLoadConf.loadUser)
                .diskCacheSize(31457280)
                .build()
        ImageLoader.getInstance().init(libConfig)
    }
}