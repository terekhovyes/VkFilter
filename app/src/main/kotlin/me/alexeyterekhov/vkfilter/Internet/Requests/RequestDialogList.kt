package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataCache.DialogListCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.Internet.JSONParser
import org.json.JSONObject
import java.util.*

class RequestDialogList(val offset: Int, val count: Int) : Request("execute.detailedDialogs") {
    init {
        params["offset"] = offset
        params["count"] = count
    }
    override fun handleResponse(json: JSONObject) {
        val jsonUserList = json.getJSONObject("response").getJSONArray("user_info")
        val jsonDialogList = json.getJSONObject("response").getJSONArray("items")

        // Put users in cache
        (JSONParser parseUsers jsonUserList).forEach { UserCache putUser it }

        // Parse dialog list
        val dialogs = JSONParser parseDialogs jsonDialogList
        val prevSnap = DialogListCache.getSnapshot()
        val mergedList = Vector<Dialog>()
        mergedList.addAll(prevSnap.dialogs.subList(0, offset))
        mergedList.addAll(dialogs)
        val newSnap = DialogListSnapshot(System.currentTimeMillis(), mergedList)

        DialogListCache updateSnapshot newSnap
        UserCache.dataUpdated()
    }
}