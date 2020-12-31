package com.iammert.library.readablebottombar

import android.graphics.drawable.Drawable

data class BottomBarItemConfig(
        val id: Int,
        val text: String,
        val drawable: Drawable,
        val index: Int
)