package me.alexeyterekhov.vkfilter.GUI.ChatActivity.AttachmentsList

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedImages
import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import java.util.LinkedList

public class AttachmentsAdapter(
        val recycler: RecyclerView,
        val dialogId: String,
        val isChat: Boolean
) :
        RecyclerView.Adapter<ImageHolder>(),
        AttachedImages.AttachedImageListener
{
    val inflater = LayoutInflater.from(AppContext.instance)
    val data = LinkedList<ImageUpload>()

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val item = data[position]

        holder.image.setImageBitmap(loadScaledImage(item.filePath, 500))

        when (item.state) {
            ImageUpload.STATE_WAIT -> {
                with (holder.progressBar) {
                    setVisibility(View.VISIBLE)
                    setMax(0)
                    setProgress(0)
                }
            }
            ImageUpload.STATE_IN_PROCESS -> {
                with (holder.progressBar) {
                    setVisibility(View.VISIBLE)
                    setMax(100)
                    setProgress(item.loadedPercent)
                }
            }
            ImageUpload.STATE_UPLOADED -> holder.progressBar.setVisibility(View.INVISIBLE)
        }

        holder.image setOnClickListener {
            AttachedCache.get(dialogId, isChat).images.removeImage(item.filePath)
        }
    }

    override fun getItemCount() = data.count()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ImageHolder? {
        return ImageHolder(inflater.inflate(R.layout.item_attached_image, parent, false))
    }

    override fun onAdd(image: ImageUpload) {
        data add image
        notifyItemInserted(data.size() - 1)
    }

    override fun onRemoved(image: ImageUpload) {
        val index = data indexOf image
        data remove index
        notifyItemRemoved(index)
    }

    override fun onProgress(image: ImageUpload, percent: Int) {
        val index = data indexOf image
        if (index != -1) {
            val man = recycler.getLayoutManager() as LinearLayoutManager
            if (index >= man.findFirstVisibleItemPosition()
                && index <= man.findLastVisibleItemPosition()
            ) {
                val view = man.findViewByPosition(index)
                val holder = recycler.getChildViewHolder(view) as ImageHolder
                holder.progressBar.setProgress(percent)
            }
        }
    }

    override fun onFinish(image: ImageUpload) {
        val index = data indexOf image
        if (index != -1) {
            val man = recycler.getLayoutManager() as LinearLayoutManager
            if (index >= man.findFirstVisibleItemPosition()
                    && index <= man.findLastVisibleItemPosition()
            ) {
                val view = man.findViewByPosition(index)
                val holder = recycler.getChildViewHolder(view) as ImageHolder
                holder.progressBar.setVisibility(View.INVISIBLE)
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
}