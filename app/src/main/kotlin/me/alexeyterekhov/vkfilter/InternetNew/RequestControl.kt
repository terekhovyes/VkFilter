package me.alexeyterekhov.vkfilter.InternetNew

import android.util.Log
import me.alexeyterekhov.vkfilter.InternetNew.Requests.Request
import me.alexeyterekhov.vkfilter.Util.Chef
import me.alexeyterekhov.vkfilter.Util.Recipe
import org.json.JSONObject

public object RequestControl {
    private val LOG_TAG = "RequestControl"

    fun addForeground(request: Request) = addRequest(request, RequestRecipes.foregroundRecipe)
    fun addBackground(request: Request) = addRequest(request, RequestRecipes.backgroundRecipe)
    fun addBackgroundOrdered(request: Request) = addRequest(request, RequestRecipes.backgroundOrderedRecipe)
    fun pause() = Chef.denyCooking(RequestRecipes.foregroundRecipe)
    fun resume() {
        checkSdkInitialized()
        Chef.allowCooking(RequestRecipes.foregroundRecipe)
    }

    private fun addRequest(request: Request, recipe: Recipe<Request, JSONObject>) {
        checkSdkInitialized()
        Log.d(LOG_TAG, ">>> Request [${request.getServerFunName()}]]")
        Chef.cook(recipe, request)
    }

    private fun checkSdkInitialized() {
        if (VkSdkInitializer.isNull())
            VkSdkInitializer.init()
    }
}

/*

public object RunFun {

    public fun friendList(offset: Int, count: Int) {
        val params = VKParameters()
        params["count"] = count
        params["offset"] = offset
        params["order"] = "hints"
        params["fields"] = "name,sex,photo_max,last_seen"
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.friendList, params))
    }

    public fun userInfo(ids: Collection<String>) {
        val params = VKParameters()
        params["user_ids"] = ids.join(separator = ",")
        params["fields"] = "name,sex,online,photo_max,last_seen"
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.userInfo, params))
    }

    public fun chatInfo(ids: Collection<String>) {
        val params = VKParameters()
        params["chat_ids"] = ids.join(separator = ",")
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.chatInfo, params))
    }

    public fun sendMessageOld(msg: MessageForSending) {
        val params = VKParameters()
        params["message"] = msg.text
        params[if (msg.isChat) "chat_id" else "user_id"] = msg.dialogId
        params["guid"] = System.currentTimeMillis()
        VkRequestControl.addOrderImportantRequest(VkRequestBundle(VkFun.sendMessageOld, params))
    }

    public fun sendMessage(msg: MessageNew, dialogId: String, isChat: Boolean): Long {
        val params = VKParameters()
        params["message"] = msg.text
        params[if (isChat) "chat_id" else "user_id"] = dialogId
        val guid = System.currentTimeMillis()
        params["guid"] = System.currentTimeMillis()
        msg.sentId = guid
        VkRequestControl.addOrderImportantRequest(VkRequestBundle(VkFun.sendMessage, params))
        return guid
    }

    public fun markIncomesAsReadOld(id: String, chat: Boolean) {
        val messagePack = MessageCacheOld.getDialog(id, chat)
        if (messagePack.messages any {!it.isOut && !it.isRead}) {
            val ids = messagePack.messages filter { !it.isOut && !it.isRead } map { it.id.toString() }
            val params = VKParameters()
            params["message_ids"] = ids.joinToString(separator = ",")
            val additionalParams = Bundle()
            additionalParams.putString("id", id)
            additionalParams.putBoolean("chat", chat)
            VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.markIncomesAsReadOld, params, additionalParams))
        }
    }

    public fun markIncomesAsRead(dialogId: String, isChat: Boolean) {
        val messages = MessageCaches.getCache(dialogId, isChat).getMessages()
        val notRead = messages filter { it.isIn && it.isNotRead }
        if (notRead.isNotEmpty()) {
            val lastReadId = (notRead maxBy { it.sentId })!!.sentId
            val ids = notRead map { it.sentId.toString() }
            val params = VKParameters()
            params["message_ids"] = ids.joinToString(separator = ",")
            val additionalParams = Bundle()
            additionalParams.putString("dialogId", dialogId)
            additionalParams.putBoolean("isChat", isChat)
            additionalParams.putLong("lastReadId", lastReadId)
            VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.markIncomesAsRead, params, additionalParams))
        }
    }

    public fun registerGCM(id: String) {
        val params = VKParameters()
        params["token"] = id
        params["subscribe"] = "msg"
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.registerGCM, params))
    }

    public fun unregisterGCM(id: String) {
        val params = VKParameters()
        params["token"] = id
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.unregisterGCM, params))
    }

    public fun notificationInfo(messageId: String) {
        val params = VKParameters()
        params["message_id"] = messageId
        VkRequestControl.addUnstoppableRequest(VkRequestBundle(VkFun.notificationInfo, params))
    }

    public fun getDialogPartners(id: Long, isChat: Boolean) {
        val params = VKParameters()
        params["id"] = id
        params["chat"] = if (isChat) 1 else 0
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.getDialogPartners, params))
    }

    public fun getVideoUrls(dialogId: String, isChat: Boolean, ids: Collection<String>) {
        val params = VKParameters()
        params["video_ids"] = "'${ids.joinToString(separator = ",")}'"
        val additional = Bundle()
        additional.putString("id", dialogId)
        additional.putBoolean("chat", isChat)
        VkRequestControl.addStoppableRequest(VkRequestBundle(VkFun.videoUrls, params, additional))
    }
}
 */