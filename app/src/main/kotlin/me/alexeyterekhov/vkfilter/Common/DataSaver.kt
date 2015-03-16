package me.alexeyterekhov.vkfilter.Common

import java.util.HashMap


public object DataSaver {
    private val data = HashMap<String, Any>()

    public fun putObject(key: String, value: Any?) {
        if (value != null)
            data.put(key, value)
    }

    public fun removeObject(key: String): Any? {
        if (data.containsKey(key))
            return data.remove(key)
        return null
    }

    public fun contains(key: String): Boolean = data containsKey key
}