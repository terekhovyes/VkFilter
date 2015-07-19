package me.alexeyterekhov.vkfilter.Util

import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.GUI.Common.TripleSwitchView
import me.alexeyterekhov.vkfilter.R


object FilterStates {
    fun filterToSwitch(filterState: Int) = when (filterState) {
        VkFilter.STATE_ALLOWING -> TripleSwitchView.STATE_RIGHT
        VkFilter.STATE_BLOCKING -> TripleSwitchView.STATE_LEFT
        else -> TripleSwitchView.STATE_MIDDLE
    }
    fun switchToFilter(switchState: Int) = when (switchState) {
        TripleSwitchView.STATE_RIGHT -> VkFilter.STATE_ALLOWING
        TripleSwitchView.STATE_LEFT -> VkFilter.STATE_BLOCKING
        else -> VkFilter.STATE_DISABLED
    }
    fun filterToString(filterState: Int) = when (filterState) {
        VkFilter.STATE_ALLOWING -> R.string.edit_filter_label_filter_allowing
        VkFilter.STATE_BLOCKING -> R.string.edit_filter_label_filter_blocking
        else -> R.string.edit_filter_label_filter_disabled
    }
    fun switchToString(switchState: Int) = filterToString(switchToFilter(switchState))
}