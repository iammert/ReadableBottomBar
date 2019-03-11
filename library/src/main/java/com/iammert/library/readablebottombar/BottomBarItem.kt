package com.iammert.library.readablebottombar

import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import com.iammert.library.readablebottombar.ReadableBottomBar.ItemType

data class BottomBarItem(
    val index: Int,
    val text: String,
    val textSize: Float,
    @ColorInt val textColor: Int,
    val drawable: Drawable,
    val type: ItemType
)