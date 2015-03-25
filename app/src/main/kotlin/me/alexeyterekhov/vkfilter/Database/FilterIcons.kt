package me.alexeyterekhov.vkfilter.Database

import me.alexeyterekhov.vkfilter.R


public object FilterIcons {
    private val ID_TO_RES = hashMapOf(
            1 to R.drawable.group_icon_01,
            2 to R.drawable.group_icon_02,
            3 to R.drawable.group_icon_03,
            4 to R.drawable.group_icon_04,
            5 to R.drawable.group_icon_05
    )

    private val RES_TO_TRANSPARENT_RES = hashMapOf(
            R.drawable.group_icon_01 to R.drawable.group_icon_transp_01,
            R.drawable.group_icon_02 to R.drawable.group_icon_transp_02,
            R.drawable.group_icon_03 to R.drawable.group_icon_transp_03,
            R.drawable.group_icon_04 to R.drawable.group_icon_transp_04,
            R.drawable.group_icon_05 to R.drawable.group_icon_transp_05
    )

    fun iconCount() = ID_TO_RES.size()

    fun resourceToId(res: Int): Int {
        return if (ID_TO_RES containsValue res)
            keyOf(res)
        else if (RES_TO_TRANSPARENT_RES containsValue res) {
            val r = keyOfTr(res)
            keyOf(r)
        } else
            keyOf(R.drawable.group_icon_01)
    }

    fun idToResource(id: Int): Int {
        return if (ID_TO_RES containsKey id)
            ID_TO_RES[id]
        else
            R.drawable.group_icon_01
    }

    fun idToTransparentResource(id: Int) = RES_TO_TRANSPARENT_RES[idToResource(id)]

    private fun keyOf(v: Int) = (ID_TO_RES.entrySet() first { it.value == v }).key
    private fun keyOfTr(v: Int) = (RES_TO_TRANSPARENT_RES.entrySet() first { it.value == v }).key
}