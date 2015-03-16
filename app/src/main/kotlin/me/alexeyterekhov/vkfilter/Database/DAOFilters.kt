package me.alexeyterekhov.vkfilter.Database

import com.activeandroid.Model
import com.activeandroid.query.Select

public object DAOFilters {
    fun loadVkFilters(): List<VkFilter> {
        return Select().all()
                    .from(javaClass<VkFilter>())
                    .orderBy("ListOrder")
                    .execute()
    }

    fun loadVkFilterById(id: Long): VkFilter {
        return Model.load(javaClass<VkFilter>(), id)
    }

    fun saveFilter(f: VkFilter) {
        f.save()
    }

    fun deleteFilter(f: VkFilter) {
        f.delete()
    }
}