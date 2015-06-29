package me.alexeyterekhov.vkfilter.GUI.PhotoViewerActivity

import android.os.Bundle
import com.nostra13.universalimageloader.core.ImageLoader
import it.sephiroth.android.library.imagezoom.ImageViewTouch
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.ImageLoadConf


public class PhotoViewerActivity: VkActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photoview)
        val intent = getIntent()
        val url = intent.getStringExtra("photo_url")
        val view = findViewById(R.id.photo) as ImageViewTouch
        view.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN)
        ImageLoader.getInstance().displayImage(url, view, ImageLoadConf.loadFullscreenImage)
    }
}