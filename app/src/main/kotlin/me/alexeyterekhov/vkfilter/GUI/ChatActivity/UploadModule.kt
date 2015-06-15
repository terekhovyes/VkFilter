package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.app.Activity
import android.content.Intent
import me.alexeyterekhov.vkfilter.DataCache.AttachedCache.AttachedCache
import me.alexeyterekhov.vkfilter.Util.FileUtils

class UploadModule(val activity: ChatActivity) {
    companion object {
        val CODE_CHOOSE_IMAGES = 100
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_CHOOSE_IMAGES && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                val filePath = FileUtils.getPath(activity, data.getData())
                AttachedCache
                        .get(activity.launchParameters.dialogId(), activity.launchParameters.isChat())
                        .images
                        .putImage(filePath)
            }
        }
    }
}