package me.alexeyterekhov.vkfilter.Internet.Upload

import android.os.Handler
import me.alexeyterekhov.vkfilter.Data.Entities.CurrentData.AttachedImage
import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.UpdateHandlers.UpdateHandler
import me.alexeyterekhov.vkfilter.Data.Utils.ImageUploadUtil
import me.alexeyterekhov.vkfilter.Util.Chef
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object UploadImageRecipeNew {
    private val boundary = "*****"
    private val uploadName = "photo"
    private val endOfStr = "\r\n"
    private val twoHyphens = "--"

    private val handler = Handler()

    val recipe = Chef
            .createRecipe<Pair<DialogId, AttachedImage>, String>()
            .cookThisWay(uploadAndGetUrl())
            .serveThisWay(handleUploadedImage())
            .cleanUpThisWay(handlerExceptions())
            .ifCookingFail(Chef.COOK_AGAIN_RIGHT_NOW)
            .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
            .waitAfterCookingFail(500)
            .create()

    private fun uploadAndGetUrl(): (Pair<DialogId, AttachedImage>) -> String = { pair ->
        val upload = pair.second

        if (upload.canceled || getUploadUrl(upload) == "") {
            ""
        } else {
            val file = loadFile(upload.filePath)
            val uploadUrl = getUploadUrl(upload)

            // Setup connection
            val connection = URL(uploadUrl).openConnection() as HttpURLConnection
            with (connection) {
                useCaches = false
                doOutput = true
                requestMethod = "POST"
                setRequestProperty("Connection", "Keep-Alive")
                setRequestProperty("Cache-Control", "no-cache")
                setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
                setChunkedStreamingMode(1024 * 1024)
            }

            // Create control stream
            val transferListener = object : ControlOutputStream.TransferListener {
                private val totalByteCount = file.size
                private var currentPercent = 0
                private val notifier = Runnable {
                    upload.onProgress(currentPercent)
                    UpdateHandler.attachments.progressImageAttachment(pair.first, upload, currentPercent)
                }

                override fun transferred(byteCount: Long) {
                    val percent = Math.min(
                            100,
                            (byteCount / totalByteCount.toDouble() * 100).toInt()
                    )
                    if (percent > currentPercent) {
                        currentPercent = percent
                        handler.post(notifier)
                    }
                }
            }
            val controlStream = ControlOutputStream(
                    outputStream = connection.outputStream,
                    listener = transferListener
            )

            // Write data
            upload.imageUploadState = AttachedImage.Uploading(uploadUrl, controlStream)
            with (DataOutputStream(controlStream)) {
                writeBytes(twoHyphens + boundary + endOfStr)
                val fileName = getNameFromPath(upload.filePath)
                writeBytes("Content-Disposition: form-data; name=\"$uploadName\";filename=\"$fileName\"$endOfStr");
                writeBytes(endOfStr)
                try {
                    controlStream.startCounting()
                    controlStream.write(file)
                    controlStream.stopCounting()
                    writeBytes(endOfStr)
                    writeBytes(twoHyphens + boundary + twoHyphens + endOfStr)
                } catch (e: ControlOutputStream.CancelException) {
                    // Nothing to do
                } finally {
                    flush()
                    close()
                }
            }

            if (upload.canceled) {
                connection.disconnect()
                ""
            } else {
                // Get response
                val responseStream = BufferedInputStream(connection.inputStream)
                val responseReader = BufferedReader(InputStreamReader(responseStream))
                var line: String? = responseReader.readLine()
                val builder = StringBuilder()
                while (line != null) {
                    builder.append(line).append("\n")
                    line = responseReader.readLine()
                }
                responseReader.close()
                responseStream.close()
                connection.disconnect()

                // Get photo string
                val str = builder.toString()
                if (str.isEmpty())
                    throw Exception()
                str
            }
        }
    }

    private fun handleUploadedImage(): (Pair<DialogId, AttachedImage>, String) -> Unit = { pair, result ->
        val dialogId = pair.first
        val upload = pair.second
        if (result != "" && !upload.canceled) {
            val json = JSONObject(result)
            val photoParam = json.getString("photo")
            val serverParam = json.getInt("server")
            val hashParam = json.getString("hash")
            upload.imageUploadState = AttachedImage.Uploaded(photoParam, serverParam, hashParam)
            ImageUploadUtil.upload(dialogId, upload)
        }
    }

    private fun handlerExceptions(): (Pair<DialogId, AttachedImage>, Exception) -> Unit = { upload, exc ->
        exc.printStackTrace()
    }

    private fun getNameFromPath(path: String): String {
        val stubName = "image.jpg"
        return if (path.contains("/")
                && path.contains(".")
                && path.lastIndexOf(".") > path.lastIndexOf("/")
        ) {
            val name = path.substring(path.lastIndexOf("/") + 1)
            if (name.length < 3)
                stubName
            else
                name
        } else
            stubName
    }

    private fun loadFile(pathToFile: String): ByteArray {
        val file = File(pathToFile)
        val size = file.length().toInt()
        val bytes = ByteArray(size.toInt())
        with (BufferedInputStream(FileInputStream(file))) {
            read(bytes, 0, size)
            close()
        }
        return bytes
    }

    private fun getUploadUrl(image: AttachedImage): String {
        if (image.imageUploadState is AttachedImage.HasUploadUrl)
            return (image.imageUploadState as AttachedImage.HasUploadUrl).uploadUrl
        if (image.imageUploadState is AttachedImage.Uploading)
            return (image.imageUploadState as AttachedImage.Uploading).uploadUrl
        return ""
    }
}