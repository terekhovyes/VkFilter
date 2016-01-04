package me.alexeyterekhov.vkfilter.Internet.Upload

import java.io.FilterOutputStream
import java.io.OutputStream

class ControlOutputStream(
        outputStream: OutputStream,
        val listener: ControlOutputStream.TransferListener
) : FilterOutputStream(outputStream) {
    private var transferred = 0L
    private var cancel = false
    private var counting = false

    override fun write(buffer: ByteArray?, offset: Int, length: Int) {
        if (cancel)
            interrupt()
        super.write(buffer, offset, length)
        if (counting) {
            transferred += length
            listener.transferred(transferred)
        }
    }

    override fun write(oneByte: Int) {
        if (cancel)
            interrupt()
        super.write(oneByte)
        if (counting) {
            transferred += 1
            listener.transferred(transferred)
        }
    }

    override fun write(buffer: ByteArray?) {
        if (cancel)
            interrupt()
        super.write(buffer)
        if (counting) {
            transferred += buffer?.size ?: 0
            listener.transferred(transferred)
        }
    }

    private fun interrupt() = throw CancelException()

    fun cancelStreaming() { cancel = true }
    fun startCounting() { counting = true }
    fun stopCounting() { counting = false }

    interface TransferListener {
        fun transferred(byteCount: Long)
    }

    class CancelException : Exception("Was canceled")
}