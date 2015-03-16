package me.alexeyterekhov.vkfilter.GUI.PhotoViewerActivity

import android.app.Activity
import android.os.Bundle
import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher
import me.alexeyterekhov.vkfilter.R
import com.nostra13.universalimageloader.core.ImageLoader


public class PhotoViewerActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photoview)
        val intent = getIntent()
        val url = intent.getStringExtra("photo_url")
        val view = findViewById(R.id.photo) as PhotoView
        PhotoViewAttacher(view)
        ImageLoader.getInstance().displayImage(url, view)
    }
}