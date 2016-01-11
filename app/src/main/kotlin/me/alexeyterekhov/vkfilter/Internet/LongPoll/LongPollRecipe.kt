package me.alexeyterekhov.vkfilter.Internet.LongPoll

import android.text.TextUtils
import android.util.Log
import me.alexeyterekhov.vkfilter.Internet.JSONParser
import me.alexeyterekhov.vkfilter.Util.Chef
import me.alexeyterekhov.vkfilter.Util.Recipe
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object LongPollRecipe {
    private val LOG_TAG = "LongPollRecipe"
    val recipe = buildRecipe()

    private fun buildRecipe(): Recipe<String, JSONObject> {
        return Chef
                .createRecipe<String, JSONObject>()
                .cookThisWay { url -> cookUrl(url) }
                .serveThisWay { url, jsonObject -> serveJson(jsonObject) }
                .cleanUpThisWay { url, exception -> cleanUpErrors(url, exception) }
                .ifCookingFail(Chef.COOK_AGAIN_AFTER_OTHERS)
                .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
                .waitAfterCookingFail(3000)
                .create()
    }

    private fun cookUrl(url: String): JSONObject {
        val stringResponse = requestAsString(url)
        return JSONObject(stringResponse)
    }

    private fun serveJson(json: JSONObject) {
        Log.d(LOG_TAG, "LongPoll request complete: " + json.toString())

        val fail = JSONParser.parseLongPollFailParam(json)
        if (fail != null) {
            if (fail.toInt() == 1) {
                val newTs = JSONParser.parseLongPollTsParam(json)
                LongPollControl.loop(newTs)
            } else {
                LongPollControl.start()
            }
        } else {
            val newTs = JSONParser.parseLongPollTsParam(json)
            val events = JSONParser.parseLongPollEvents(json.getJSONArray("updates"))

            events.forEach { LongPollControl.eventBus().post(it) }
            LongPollControl.loop(newTs)
        }
    }

    private fun cleanUpErrors(url: String, e: Exception) {
        Log.d(LOG_TAG, "LongPoll request error")
    }

    private fun requestAsString(url: String): String {
        // Setup connection
        val connection = URL(url).openConnection() as HttpURLConnection
        with (connection) {
            useCaches = false
            doOutput = false
            requestMethod = "POST"
            setRequestProperty("Connection", "Keep-Alive")
            setRequestProperty("Cache-Control", "no-cache")
        }

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

        val response = builder.toString()
        if (TextUtils.isEmpty(response))
            throw Exception()

        return response
    }
}