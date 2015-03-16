package me.alexeyterekhov.vkfilter.Internet.VkApi

import java.util.Collections
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer

public object VkRequestControl {
    private fun checkSdkInitialized() {
        if (VkSdkInitializer.isNull())
            VkSdkInitializer.init()
    }

    public fun addRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        VkTask.instance.handle(Collections.singleton(request))
    }

    public fun addUnstoppableRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        VkTask.unstoppableInstance.handle(Collections.singleton(request))
    }

    public fun pause() {
        val task = VkTask.instance
        if (task.isRunning() && !task.willPause())
            task.pauseAfterCurrentSource()
    }

    public fun resume() {
        checkSdkInitialized()
        val task = VkTask.instance
        if (!task.isRunning() || task.willPause())
            task.resumeHandling()
    }
}