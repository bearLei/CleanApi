package com.dn.cleanapi.entity


data class MenuJunkParent(
    val name: String,
    /**
     * 大小
     */
    val size: Long,
    /**
     * 是否展开
     */
    var isExpand: Boolean,

    /**
     * 是否选中
     */
    var select: Boolean,
    var childList: List<MenuJunkChild>? = null,
) {
}