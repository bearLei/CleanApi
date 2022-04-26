package com.dn.cleanapi.entity

import com.mckj.api.entity.AppJunk



data class MenuJunkChild(
    /**
     * 是否选中
     */
    var select: Boolean,
    val iJunkEntity: AppJunk,
    val parent: MenuJunkParent
)