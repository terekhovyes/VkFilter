package me.alexeyterekhov.vkfilter.Data.Entities.CurrentData

open class LongUpload {
    public var canceled = false
    public var state = UploadState.WAIT
    public var loadedPercent = 0

    fun onStartUpload() {
        state = UploadState.IN_PROCESS
    }

    fun onProgress(percent: Int) {
        loadedPercent = percent
    }

    fun onCancel() {
        state = UploadState.WAIT
    }

    fun onFinishUpload() {
        state = UploadState.UPLOADED
    }

    public enum class UploadState {
        WAIT,
        IN_PROCESS,
        UPLOADED
    }
}