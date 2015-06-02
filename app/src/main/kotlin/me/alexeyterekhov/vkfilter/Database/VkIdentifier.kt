package me.alexeyterekhov.vkfilter.Database

import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table


@Table(name = "VKFilterIdentifiers")
public class VkIdentifier(): Model() {
    companion object {
        val TYPE_USER = 1
        val TYPE_CHAT = 2
    }

    @Column(name = "Identifier")
    var id: Long = 0

    @Column(name = "Type")
    var type: Int = TYPE_USER

    @Column(name = "Owner")
    var ownerFilter: VkFilter? = null
}