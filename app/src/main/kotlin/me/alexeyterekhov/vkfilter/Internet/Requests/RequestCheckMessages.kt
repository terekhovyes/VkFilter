package me.alexeyterekhov.vkfilter.Internet.Requests

import org.json.JSONObject
import java.util.*

class RequestCheckMessages(val messageIds: List<Long>, val resultHandler: (List<Long>) -> Unit)
: Request("execute.checkMessages") {
    init {
        params["message_ids"] = messageIds.joinToString(separator = ",")
    }

    override fun handleResponse(json: JSONObject) {
        val readIds = json.getJSONArray("response")
        val collection = LinkedList<Long>()
        for (i in 0..readIds.length() - 1)
            collection.add(readIds.getLong(i))
        resultHandler(collection)
    }
}