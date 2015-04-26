package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.content.Intent
import android.net.Uri
import android.support.v7.app.ActionBarActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.Common.DateFormat
import me.alexeyterekhov.vkfilter.Common.ImageLoadConf
import me.alexeyterekhov.vkfilter.Common.TextFormat
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.Attachments
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.DocAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.ImageAttachment
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.VideoAttachment
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
            .plus(inflateVideos(attachments.videos, inflater, root))
            .plus(inflateDocs(attachments.documents, inflater, root))
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

    fun inflateVideos(videos: List<VideoAttachment>, inflater: LayoutInflater, root: ViewGroup): List<View> {
        val loader = ImageLoader.getInstance()
        return videos map {
            val view = inflater.inflate(R.layout.message_video, root, false)
            val targetRatio = maxViewWidth / maxViewHeight.toDouble()
            val realRatio = 4 / 3.0
            val params = view.getLayoutParams()
            if (realRatio > targetRatio) {
                params.width = maxViewWidth
                params.height = (maxViewWidth / realRatio).toInt()
            } else {
                params.width = (maxViewHeight * realRatio).toInt()
                params.height = maxViewHeight
            }

            // Video preview
            val preview = view.findViewById(R.id.preview) as ImageView
            if (it.previewUrl != "")
                loadImage(loader, preview, it.previewUrl)
            else
                preview.setImageResource(R.drawable.stub_video)

            // Duration
            val duration = view.findViewById(R.id.duration) as TextView
            duration setText (DateFormat duration it.durationSec.toLong())

            // Click listener
            val playImage = view.findViewById(R.id.playImage)
            val url = it.playerUrl
            if (it.playerUrl != "") {
                preview.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    activity startActivity intent
                }
                playImage setVisibility View.VISIBLE
            } else {
                playImage setVisibility View.INVISIBLE
            }

            view
        }
    }

    fun inflateDocs(docs: Collection<DocAttachment>, inflater: LayoutInflater, root: ViewGroup): List<View> {
        return docs map {
            val view = inflater.inflate(R.layout.message_document, root, false)

            val title = view.findViewById(R.id.title) as TextView
            title setText it.title

            val size = view.findViewById(R.id.docSize) as TextView
            size setText (TextFormat size it.sizeInBytes)

            val url = it.url
            view setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity startActivity intent
            }

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