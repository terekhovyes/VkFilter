package me.alexeyterekhov.vkfilter.GUI.PhotoViewerActivity

import android.os.Bundle
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.Common.ImageLoadConf
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.R
import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher


public class PhotoViewerActivity: VkActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photoview)
        val intent = getIntent()
        val url = intent.getStringExtra("photo_url")
        val view = findViewById(R.id.photo) as PhotoView
        PhotoViewAttacher(view)
        ImageLoader.getInstance().displayImage(url, view, ImageLoadConf.loadImage)
    }
}