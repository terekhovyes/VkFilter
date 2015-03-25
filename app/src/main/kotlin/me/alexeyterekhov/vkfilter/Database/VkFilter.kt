package me.alexeyterekhov.vkfilter.Database

import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table
import java.util.Vector

[Table(name = "VKFilters")]
public class VkFilter(): Model() {
    companion object {
        val STATE_DISABLED = 0
        val STATE_ALLOWING = 1
        val STATE_BLOCKING = 2
    }

    private var cached = false
    private val cachedIds = Vector<VkIdentifier>()

    [Column(name = "FilterName")]
    var filterName: String = ""

    [Column(name = "FilterIcon")]
    private var iconId: Int = 1

    [Column(name = "ListOrder")]
    var listOrder: Int = 9000

    [Column(name = "EnableState")]
    var state: Int = STATE_DISABLED

    fun setIconResource(res: Int) { iconId = FilterIcons.resourceToId(res) }
    fun getIconResource() = FilterIcons.idToResource(iconId)
    fun getIconTransparentBackgroundResource() = FilterIcons.idToTransparentResource(iconId)

    fun invalidateCache() { cached = false }
    fun identifiers(): List<VkIdentifier> {
        return if (cached)
            cachedIds
        else {
            cachedIds.clear()
            cachedIds addAll getMany(javaClass<VkIdentifier>(), "Owner")
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