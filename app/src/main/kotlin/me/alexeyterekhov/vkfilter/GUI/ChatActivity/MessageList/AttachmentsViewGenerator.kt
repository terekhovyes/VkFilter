package me.alexeyterekhov.vkfilter.GUI.ChatActivity.MessageList

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Attachments.*
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.GUI.PhotoViewerActivity.PhotoViewerActivity
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DateFormat
import me.alexeyterekhov.vkfilter.Util.ImageLoadConf
import me.alexeyterekhov.vkfilter.Util.TextFormat


class AttachmentsViewGenerator(
        val maxViewWidth: Int,
        val maxViewHeight: Int,
        val shownUrls: MutableSet<String>,
        val activity: AppCompatActivity
) {
    fun inflate(attachments: Attachments, inflater: LayoutInflater, root: ViewGroup, darkColors: Boolean = false): List<View> {
        return inflateImages(attachments.images, inflater, root)
            .plus(inflateVideos(attachments.videos, inflater, root))
            .plus(inflateAudios(attachments.audios, inflater, root))
            .plus(inflateDocs(attachments.documents, inflater, root))
            .plus(inflateLinks(attachments.links, inflater, root))
            .plus(inflateWalls(attachments.walls, inflater, root))
            .plus(inflateForwardMessages(attachments.messages, inflater, root, darkColors))
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

    fun inflateAudios(audios: Collection<AudioAttachment>, inflater: LayoutInflater, root: ViewGroup): List<View> {
        return audios map {
            val view = inflater.inflate(R.layout.message_audio, root, false)

            val title = view.findViewById(R.id.title) as TextView
            title setText it.title
            val artist = view.findViewById(R.id.artist) as TextView
            artist setText it.artist
            val duration = view.findViewById(R.id.duration) as TextView
            duration setText (DateFormat duration it.durationInSec.toLong())

            val url = it.url
            view setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity startActivity intent
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

    fun inflateLinks(links: Collection<LinkAttachment>, inflate: LayoutInflater, root: ViewGroup): List<View> {
        return links map {
            val view = inflate.inflate(R.layout.message_link, root, false)
            val titleView = view.findViewById(R.id.title) as TextView
            val urlView = view.findViewById(R.id.url) as TextView
            val url = it.url

            titleView setText it.title
            urlView setText url

            view setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity startActivity intent
            }

            view
        }
    }

    fun inflateWalls(walls: Collection<WallAttachment>, inflate: LayoutInflater, root: ViewGroup): List<View> {
        return walls map {
            val view = inflate.inflate(R.layout.message_wall, root, false)
            view
        }
    }

    fun inflateForwardMessages(
            messages: Collection<Message>,
            inflater: LayoutInflater,
            root: ViewGroup,
            darkColors: Boolean
    ): List<View> {
        return messages map {
            val holder = messageToView(it, inflater, root, darkColors)
            holder.view
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

    private fun messageToView(m: Message, i: LayoutInflater, root: ViewGroup, darkColors: Boolean = false): ForwardMessageHolder {
        val view = i.inflate(R.layout.item_fwd_message, root, false)
        val holder = ForwardMessageHolder(view)
        if (darkColors)
            holder.setDarkColors()
        with (holder) {
            if (UserCache.contains(m.senderId))
                fillUserInfo(UserCache.getUser(m.senderId)!!)
            else
                fillUserNotLoaded()
            setDate(m.sentTimeMillis)
            setMessageText(m.text)
            inflate(m.attachments, i, attachmentsLayout, darkColors) forEach {
                addAttachment(it)
            }
        }
        return holder
    }
}