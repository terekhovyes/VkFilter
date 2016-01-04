package me.alexeyterekhov.vkfilter.Util

import java.util.*


public object DataSaver {
    private val data = HashMap<String, Any>()

    public fun putObject(key: String, value: Any?) {
        if (value != null)
            data.put(key, value)
    }

    public infix fun removeObject(key: String): Any? {
        if (data.containsKey(key))
            return data.remove(key)
        return null
    }

    public infix fun contains(key: String): Boolean = data.containsKey(key)
}