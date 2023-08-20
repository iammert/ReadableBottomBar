package com.iammert.library.readablebottombar

import android.app.Activity
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class ReadableBottomBarTest {

    private val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    @Test
    fun selectedIndex_defaultsToInitialIndex() {
        val bar = createBar(initialIndex = 2)

        assertEquals(2, bar.selectedIndex)
    }

    @Test
    fun initialIndex_outOfRange_failsWithDescriptiveError() {
        val error = assertThrows(IllegalArgumentException::class.java) { createBar(initialIndex = 3) }

        assertEquals("rbb_initialIndex is 3 but the bar has 3 tabs", error.message)
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
    fun selectItem_movesIndicatorWithoutChangingItsLayout() {
        val bar = createBar()
        layout(bar)
        val indicator = bar.getChildAt(0)

        bar.selectItem(1)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ReadableBottomBar.ANIMATION_DURATION))

        assertEquals(0, indicator.left)
        assertEquals(200f, indicator.x)
    }

    @Test
    fun tappingSelectedItem_notifiesReselectListenerOnly() {
        val bar = createBar()
        layout(bar)
        val selections = mutableListOf<Int>()
        val reselections = mutableListOf<Int>()
        bar.setOnItemSelectListener { selections += it }
        bar.setOnItemReselectListener { reselections += it }

        bar.findItemViews()[0].performClick()
        bar.findItemViews()[1].performClick()
        bar.findItemViews()[1].performClick()

        assertEquals(listOf(1), selections)
        assertEquals(listOf(0, 1), reselections)
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

    @Test
    fun selectItem_beforeLayout_isAppliedOnceItemsAreDrawn() {
        val bar = createBar()

        bar.selectItem(2)
        layout(bar)

        assertEquals(2, bar.selectedIndex)
        val indicator = bar.getChildAt(0)
        assertEquals(400f, indicator.x)
    }

    @Test
    fun selectedIndex_survivesSaveAndRestore() {
        val bar = createBar().apply { id = BAR_ID }
        layout(bar)
        bar.selectItem(2)
        val container = SparseArray<Parcelable>()
        bar.saveHierarchyState(container)

        val restored = createBar().apply { id = BAR_ID }
        restored.restoreHierarchyState(parcelRoundTrip(container))
        layout(restored)

        assertEquals(2, restored.selectedIndex)
    }

    @Test
    fun restoreAfterLayout_selectsSavedItem() {
        val bar = createBar().apply { id = BAR_ID }
        layout(bar)
        bar.selectItem(1)
        val container = SparseArray<Parcelable>()
        bar.saveHierarchyState(container)

        val restored = createBar().apply { id = BAR_ID }
        layout(restored)
        restored.restoreHierarchyState(container)

        assertEquals(1, restored.selectedIndex)
    }

    private fun parcelRoundTrip(container: SparseArray<Parcelable>): SparseArray<Parcelable> {
        val parcel = Parcel.obtain()
        try {
            parcel.writeSparseArray(container as SparseArray<Any>)
            parcel.setDataPosition(0)
            @Suppress("UNCHECKED_CAST")
            return parcel.readSparseArray<Parcelable>(javaClass.classLoader) as SparseArray<Parcelable>
        } finally {
            parcel.recycle()
        }
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

    private companion object {
        const val BAR_ID = 0x7f0b0001
    }
}
