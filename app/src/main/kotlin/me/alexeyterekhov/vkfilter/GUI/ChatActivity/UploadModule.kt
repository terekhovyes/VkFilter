package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.app.Activity
import android.content.Intent
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.Util.DataSaver
import me.alexeyterekhov.vkfilter.Util.FileUtils
import java.io.File

class UploadModule(val activity: ChatActivity) {
    companion object {
        val CODE_CHOOSE_IMAGES = 100
        val CODE_CAMERA = 200
    }

    var cameraFile: File? = null

    fun onSaveState() {
        DataSaver.putObject("file", cameraFile)
    }

    fun onCreate() {
        if (DataSaver.contains("file"))
            cameraFile = DataSaver.removeObject("file") as File
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == CODE_CHOOSE_IMAGES && resultCode == Activity.RESULT_OK -> {
                if (data != null) {
                    val filePath = FileUtils.getPath(activity, data.getData())
                    if (filePath != null) {
                        AttachedCache
                                .get(activity.launchParameters.dialogId(), activity.launchParameters.isChat())
                                .images
                                .putImage(filePath)
                    }
                }
            }
            requestCode == CODE_CAMERA && resultCode == Activity.RESULT_OK -> {
                if (cameraFile != null) {
                    AttachedCache
                            .get(activity.launchParameters.dialogId(), activity.launchParameters.isChat())
                            .images
                            .putImage(cameraFile!!.getAbsolutePath())
                }
            }
        }
    }
}