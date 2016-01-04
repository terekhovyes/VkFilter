package me.alexeyterekhov.vkfilter.Database

import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table
import java.util.*

@Table(name = "VKFilters")
public class VkFilter(): Model() {
    companion object {
        val STATE_DISABLED = 0
        val STATE_ALLOWING = 1
        val STATE_BLOCKING = 2
    }

    var cached = false
    val cachedIds = Vector<VkIdentifier>()

    @Column(name = "FilterName")
    var filterName: String = ""

    @Column(name = "FilterIcon")
    var iconId: Int = 1

    @Column(name = "ListOrder")
    var listOrder: Int = 9000

    @Column(name = "EnableState")
    var state: Int = STATE_DISABLED

    fun setIcon(iconId: Int) { this.iconId = iconId }
    fun getIcon() = iconId

    fun invalidateCache() { cached = false }
    fun identifiers(): List<VkIdentifier> {
        return if (cached)
            cachedIds
        else {
            cachedIds.clear()
            cachedIds.addAll(getMany(VkIdentifier::class.java, "Owner"))
            cached = true
            cachedIds
        }
    }

    fun addIdentifier(id: VkIdentifier) {
        cached = false
        id.ownerFilter = this
        id.save()
    }
}