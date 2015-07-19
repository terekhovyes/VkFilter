package me.alexeyterekhov.vkfilter.GUI.ChatActivity.AttachmentsList

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.Attached
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedImages
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedMessagePack
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import java.util.LinkedList


class AttachmentsAdapter(val recycler: RecyclerView) : RecyclerView.Adapter<AttachmentHolder>() {
    private val inflater = LayoutInflater.from(AppContext.instance)
    private var attached: Attached? = null

    private val messages = LinkedList<AttachedMessagePack>()
    private val images = LinkedList<ImageUpload>()

    val dataListener = createDataListener()
    val uploadListener = createUploadListener()

    fun setData(attached: Attached) {
        this.attached = attached
        messages addAll attached.messages.get()
        images addAll attached.images.uploads
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return messages.count() + images.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AttachmentHolder? {
        return AttachmentHolder(inflater.inflate(R.layout.item_attached, parent, false))
    }

    override fun onBindViewHolder(holder: AttachmentHolder, position: Int) {
        val messageCount = messages.count()
        val imageCount = images.count()

        when {
            messageCount != 0 && position in 0..messageCount - 1 -> {
                val messagePack = attached!!.messages.get()[position]
                with (holder.imageProgressBar) {
                    setImageResource(R.color.m_black)
                    setMaxProgress(100)
                    setCurrentProgress(100)
                    setOnCloseListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            attached?.messages?.remove(messagePack)
                        }
                    })
                }
                holder.countText.setText(messagePack.messageIds.count().toString())
                holder.countText.setVisibility(View.VISIBLE)
                holder.labelText.setText(messagePack.title)
                holder.labelText.setVisibility(View.VISIBLE)
            }
            imageCount != 0 && position in messageCount..messageCount + imageCount - 1 -> {
                val image = attached!!.images.uploads[position - messageCount]
                with (holder.imageProgressBar) {
                    setImageBitmap(loadScaledImage(image.filePath, 500))
                    setMaxProgress(100)
                    setCurrentProgress(when (image.state) {
                        ImageUpload.STATE_WAIT -> 0
                        ImageUpload.STATE_IN_PROCESS -> image.loadedPercent
                        ImageUpload.STATE_UPLOADED -> 100
                        else -> 0
                    })
                    setOnCloseListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            attached?.images?.removeImage(image.filePath)
                        }
                    })
                }
                holder.countText.setVisibility(View.INVISIBLE)
                holder.labelText.setVisibility(View.INVISIBLE)
            }
        }
    }

    private fun loadScaledImage(path: String, sideSize: Int): Bitmap {
        // Original size
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val width = options.outWidth.toDouble()
        val height = options.outHeight.toDouble()

        // Rough compress on decoding
        val params = BitmapFactory.Options()
        params.inSampleSize = Math.max(width / sideSize, height / sideSize).toInt()
        val image = BitmapFactory.decodeFile(path, params)
        return image
    }

    private fun createUploadListener() = object : AttachedImages.AttachedImageListener {
        override fun onAdd(image: ImageUpload) {
            images add image
            notifyItemInserted(messages.count() + images.count() - 1)
        }

        override fun onRemoved(image: ImageUpload) {
            val index = images indexOf image
            images remove index
            notifyItemRemoved(messages.count() + index)
        }

        override fun onProgress(image: ImageUpload, percent: Int) {
            val index = images indexOf image
            if (index != -1) {
                val position = index + messages.count()
                val man = recycler.getLayoutManager() as LinearLayoutManager
                if (position >= man.findFirstVisibleItemPosition()
                        && position <= man.findLastVisibleItemPosition()
                ) {
                    val view = man.findViewByPosition(position)
                    val holder = recycler.getChildViewHolder(view) as AttachmentHolder
                    holder.imageProgressBar.setCurrentProgress(percent)
                }
            }
        }

        override fun onFinish(image: ImageUpload) {
            val index = images indexOf image
            if (index != -1) {
                val position = index + messages.count()
                val man = recycler.getLayoutManager() as LinearLayoutManager
                if (position >= man.findFirstVisibleItemPosition()
                        && position <= man.findLastVisibleItemPosition()
                ) {
                    val view = man.findViewByPosition(position)
                    val holder = recycler.getChildViewHolder(view) as AttachmentHolder
                    holder.imageProgressBar.setCurrentProgress(100)
                }
            }
        }
    }

    private fun createDataListener() = object : DataDepend {
        override fun onDataUpdate() {
            when {
                messages.count() != attached!!.messages.get().count() -> {
                    when {
                        messages.count() > attached!!.messages.get().count() -> {
                            val index = messages indexOfFirst {
                                !attached!!.messages.get().contains(it)
                            }
                            messages remove index
                            notifyItemRemoved(index)
                        }
                        messages.count() < attached!!.messages.get().count() -> {
                            val index = attached!!.messages.get() indexOfFirst {
                                !messages.contains(it)
                            }
                            messages.add(index, attached!!.messages.get()[index])
                            notifyItemInserted(index)
                        }
                    }
                }
            }
        }
    }
}