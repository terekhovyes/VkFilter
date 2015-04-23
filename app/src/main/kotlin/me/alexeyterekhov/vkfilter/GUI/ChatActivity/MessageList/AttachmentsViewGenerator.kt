package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.content.Intent
import android.support.v7.app.ActionBarActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.Common.ImageLoadConf
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.ImageAttachment
import me.alexeyterekhov.vkfilter.GUI.PhotoViewerActivity.PhotoViewerActivity
import me.alexeyterekhov.vkfilter.R


class AttachmentsViewGenerator(
        val maxViewWidth: Int,
        val maxViewHeight: Int,
        val shownUrls: MutableSet<String>,
        val activity: ActionBarActivity
) {
    fun inflate(attachments: Attachments, inflater: LayoutInflater, root: ViewGroup): List<View> {
        return inflateImages(attachments.images, inflater, root)
    }

    fun inflateImages(images: List<ImageAttachment>, inflater: LayoutInflater, root: ViewGroup): List<View> {
        val loader = ImageLoader.getInstance()
        return images map {
            val view = inflater.inflate(R.layout.message_image, root, false) as ImageView

            // Set correct aspect ratio
            val targetRatio = maxViewWidth / maxViewHeight.toDouble()
            val realRatio = it.width / it.height.toDouble()
            val params = view.getLayoutParams()
            if (realRatio > targetRatio) {
                params.width = maxViewWidth
                params.height = (maxViewWidth / realRatio).toInt()
            } else {
                params.width = (maxViewHeight * realRatio).toInt()
                params.height = maxViewHeight
            }

            // Open viewer on click
            // TODO Switching images
            val url = it.fullSizeUrl
            view setOnClickListener {
                val intent = Intent(AppContext.instance, javaClass<PhotoViewerActivity>())
                intent.putExtra("photo_url", url)
                activity startActivity intent
            }

            // Show image
            loadImage(loader, view, it.smallSizeUrl)

            view
        }
    }

    private fun loadImage(loader: ImageLoader, view: ImageView, url: String) {
        val conf = if (url !in shownUrls) {
            shownUrls add url
            ImageLoadConf.loadImage
        } else
            ImageLoadConf.loadImageWithoutAnim
        loader.displayImage(url, view, conf)
    }
}