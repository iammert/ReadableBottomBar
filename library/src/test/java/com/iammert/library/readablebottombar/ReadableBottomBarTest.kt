package com.iammert.library.readablebottombar

import android.app.Activity
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class ReadableBottomBarTest {

    private val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    @Test
    fun selectedIndex_defaultsToInitialIndex() {
        val bar = createBar(initialIndex = 2)

        assertEquals(2, bar.selectedIndex)
    }

    @Test
    fun selectItem_updatesSelectedIndexAndNotifiesListener() {
        val bar = createBar()
        layout(bar)
        val selections = mutableListOf<Int>()
        bar.setOnItemSelectListener(object : ReadableBottomBar.ItemSelectListener {
            override fun onItemSelected(index: Int) {
                selections += index
            }
        })

        bar.selectItem(1)

        assertEquals(1, bar.selectedIndex)
        assertEquals(listOf(1), selections)
    }

    @Test
    fun selectItem_outOfRange_throws() {
        val bar = createBar()

        assertThrows(IllegalArgumentException::class.java) { bar.selectItem(3) }
        assertThrows(IllegalArgumentException::class.java) { bar.selectItem(-1) }
    }

    @Test
    fun resize_keepsASingleSetOfItemViews() {
        val bar = createBar()
        layout(bar)

        layout(bar, width = 800)

        assertEquals(3, bar.findItemViews().size)
    }

    private fun createBar(initialIndex: Int = 0): ReadableBottomBar {
        val attrs = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.rbb_tabs, "@xml/rbb_test_tabs")
                .addAttribute(R.attr.rbb_initialIndex, initialIndex.toString())
                .build()
        return ReadableBottomBar(activity, attrs).also {
            activity.setContentView(FrameLayout(activity).apply { addView(it, FrameLayout.LayoutParams(0, 0)) })
        }
    }

    private fun layout(bar: View, width: Int = 600, height: Int = 120) {
        bar.layoutParams = bar.layoutParams.apply {
            this.width = width
            this.height = height
        }
        // One pass lays out the bar, the next one lays out the items it posted.
        repeat(2) { shadowOf(Looper.getMainLooper()).idle() }
    }

    private fun View.findItemViews(): List<BottomBarItemView> {
        if (this is BottomBarItemView) return listOf(this)
        if (this !is android.view.ViewGroup) return emptyList()
        return (0 until childCount).flatMap { getChildAt(it).findItemViews() }
    }
}
