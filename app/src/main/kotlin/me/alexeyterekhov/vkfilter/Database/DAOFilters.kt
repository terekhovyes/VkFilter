package me.alexeyterekhov.vkfilter.Database

import com.activeandroid.Model
import com.activeandroid.query.Select
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.GUI.Mock.Mocker
import java.util.*

public object DAOFilters {
    val changeListeners = Vector<DataDepend>()

    fun loadVkFilters(): List<VkFilter> {
        if (Mocker.MOCK_MODE)
            return Mocker.mockFilters()
        return Select().all()
                    .from(VkFilter::class.java)
                    .orderBy("ListOrder")
                    .execute()
    }

    infix fun loadVkFilterById(id: Long): VkFilter {
        return Model.load(VkFilter::class.java, id)
    }

    fun saveFilter(f: VkFilter) {
        f.save()
        for (l in changeListeners)
            l.onDataUpdate()
    }

    fun deleteFilter(f: VkFilter) {
        f.identifiers().forEach { it.delete() }
        f.delete()
    }
}