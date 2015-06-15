package me.alexeyterekhov.vkfilter.Internet.Upload

import android.os.Handler
import me.alexeyterekhov.vkfilter.DataClasses.ImageUpload
import me.alexeyterekhov.vkfilter.Util.Chef
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object UploadImageRecipe {
    private val boundary = "*****"
    private val uploadName = "photo"
    private val endOfStr = "\r\n"
    private val twoHyphens = "--"

    private val handler = Handler()

    val recipe = Chef
            .createRecipe<ImageUpload, String>()
            .cookThisWay(cookImage())
            .serveThisWay(serveImage())
            .cleanUpThisWay(cleanUpImage())
            .ifCookingFail(Chef.COOK_AGAIN_IMMEDIATELY)
            .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
            .waitAfterCookingFail(500)
            .create()

    private fun cookImage(): (ImageUpload) -> String = { upload ->
        if (upload.isCanceled()) {
            ""
        } else {
            val file = loadFile(upload.filePath)

            // Setup connection
            val connection = URL(upload.uploadUrl).openConnection() as HttpURLConnection
            with (connection) {
                setUseCaches(false)
                setDoOutput(true)
                setRequestMethod("POST")
                setRequestProperty("Connection", "Keep-Alive")
                setRequestProperty("Cache-Control", "no-cache")
                setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
                setChunkedStreamingMode(1024 * 256)
            }

            // Create control stream
            val transferListener = object : ControlOutputStream.TransferListener {
                private val totalByteCount = file.size()
                private var currentPercent = 0
                private val notifier = Runnable { upload.onProgress(currentPercent) }

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
                    outputStream = connection.getOutputStream(),
                    listener = transferListener
            )

            // Write data
            handler.post({ upload.onStartUploading(controlStream) })
            with (DataOutputStream(controlStream)) {
                writeBytes(twoHyphens + boundary + endOfStr)
                val fileName = getNameFromPath(upload.filePath)
                writeBytes("Content-Disposition: form-data; name=\"$uploadName\";filename=\"$fileName\"" + endOfStr);
                writeBytes(endOfStr)
                try {
                    controlStream.startCounting()
                    write(file)
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

            if (upload.isCanceled()) {
                connection.disconnect()
                ""
            } else {
                // Get response
                val responseStream = BufferedInputStream(connection.getInputStream())
                val responseReader = BufferedReader(InputStreamReader(responseStream))
                var line: String? = responseReader.readLine()
                val builder = StringBuilder()
                while (line != null) {
                    builder append line append "\n"
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

    private fun serveImage(): (ImageUpload, String) -> Unit = { upload, result ->
        if (!upload.isCanceled()) {
            upload.onFinishUploading(result)
        }
    }

    private fun cleanUpImage(): (ImageUpload, Exception) -> Unit = { upload, exc ->
        exc.printStackTrace()
    }

    private fun getNameFromPath(path: String): String {
        val stubName = "image.jpg"
        return if (path.contains("/")
                && path.contains(".")
                && path.lastIndexOf(".") > path.lastIndexOf("/")
        ) {
            val name = path.substring(path.lastIndexOf("/") + 1)
            if (name.length() < 3)
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
}