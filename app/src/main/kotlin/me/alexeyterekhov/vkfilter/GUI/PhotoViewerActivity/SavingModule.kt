package me.alexeyterekhov.vkfilter.GUI.PhotoViewerActivity

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.Chef
import java.io.File
import java.io.FileOutputStream

class SavingModule(val activity: PhotoViewerActivity) {
    fun saveImage(url: String) {
        Toast.makeText(activity, R.string.photo_toast_loading_started, Toast.LENGTH_SHORT).show()
        ImageLoader.getInstance().loadImage(url, object : ImageLoadingListener {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                if (loadedImage != null)
                    saveIntoDownloadsDirectory(loadedImage)
            }
            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
            }
            override fun onLoadingStarted(imageUri: String?, view: View?) {
            }
            override fun onLoadingCancelled(imageUri: String?, view: View?) {
            }
        })
    }

    private fun saveIntoDownloadsDirectory(image: Bitmap) {
        var name: String? = null
        var file: File? = null
        var fileUri: Uri? = null

        Chef.express(
                cooking = {
                    // Get downloads directory
                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!dir.exists()) {
                        val created = dir.mkdirs()
                        if (!created)
                            return@express false
                    }

                    // Saving file
                    try {
                        name = "pic${System.currentTimeMillis()}.jpg"
                        file = File(dir, name)
                        fileUri = Uri.fromFile(file)
                        val stream = FileOutputStream(file)

                        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.flush()
                        stream.close()
                    } catch (e: Exception) {
                        return@express false
                    }

                    true
                },
                serving = { successful ->
                    if (successful) {
                        Toast.makeText(activity, R.string.photo_toast_saved, Toast.LENGTH_SHORT).show()

                        if (fileUri != null) {
                            // Show in "downloads" app
                            val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            downloadManager.addCompletedDownload(name, name, true, "image/png", file!!.getAbsolutePath(), file!!.length() , true)
                        }
                    } else {
                        Toast.makeText(activity, R.string.photo_toast_failed, Toast.LENGTH_SHORT).show()
                    }
                }
        )
    }
}