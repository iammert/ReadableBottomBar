package com.iammert.library.readablebottombar

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.AttributeSetBuilder
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
    fun rtl_indicatorFollowsMirroredItems() {
        activity.applicationInfo.flags = activity.applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
        val bar = createBar(initialIndex = 1).apply { layoutDirection = View.LAYOUT_DIRECTION_RTL }
        layout(bar)
        val indicator = bar.getChildAt(0)
        val items = bar.findItemViews()

        assertEquals(items[1].left.toFloat(), indicator.x)

        bar.selectItem(0)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ReadableBottomBar.ANIMATION_DURATION))

        assertEquals(400, items[0].left)
        assertEquals(400f, indicator.x)
    }

    @Test
    fun items_exposeTextAndSelectionToAccessibilityServices() {
        val bar = createBar()
        layout(bar)
        val items = bar.findItemViews()

        bar.selectItem(2)

        assertEquals(listOf("Home", "Search", "Profile"), items.map { it.contentDescription })
        assertEquals(listOf(false, false, true), items.map { it.isSelected })
        val info = AccessibilityNodeInfoCompat.wrap(items[2].createAccessibilityNodeInfo())
        assertEquals(2, info.collectionItemInfo.columnIndex)
        assertTrue(info.collectionItemInfo.isSelected)
    }

    @Test
    fun items_fillWidthThatIsNotDivisibleByTabCount() {
        val bar = createBar()

        layout(bar, width = 601)

        assertEquals(601, bar.findItemViews().sumOf { it.width })
    }

    @Test
    fun animationDuration_isReadFromAttributes() {
        val bar = createBar(animationDuration = 100)
        layout(bar)
        val indicator = bar.getChildAt(0)

        bar.selectItem(2)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100))

        assertEquals(400f, indicator.x)
    }

    @Test
    fun textAppearance_isAppliedToItemText() {
        val bar = createBar { addAttribute(R.attr.rbb_textAppearance, "@style/RbbTest.TextAppearance") }
        layout(bar)

        val text = bar.findItemViews()[0].findViewById<TextView>(R.id.textView)

        assertEquals(20f, text.textSize)
        assertEquals(Color.RED, text.currentTextColor)
    }

    @Test
    fun textSize_overridesTextAppearance() {
        val bar = createBar {
            addAttribute(R.attr.rbb_textAppearance, "@style/RbbTest.TextAppearance")
            addAttribute(R.attr.rbb_textSize, "12px")
        }
        layout(bar)

        val text = bar.findItemViews()[0].findViewById<TextView>(R.id.textView)

        assertEquals(12f, text.textSize)
        assertEquals(Color.RED, text.currentTextColor)
    }

    @Test
    fun missingTabs_inflatesAnEmptyBar() {
        val bar = ReadableBottomBar(activity, Robolectric.buildAttributeSet().build())
        activity.setContentView(FrameLayout(activity).apply { addView(bar, FrameLayout.LayoutParams(0, 0)) })

        layout(bar)

        assertEquals(0, bar.findItemViews().size)
        assertThrows(IllegalArgumentException::class.java) { bar.selectItem(0) }
    }

    @Test
    fun setTabs_replacesItemsAndKeepsValidSelection() {
        val bar = createBar()
        layout(bar)
        bar.selectItem(1)

        bar.setTabs(R.xml.rbb_test_tabs_two)

        val items = bar.findItemViews()
        assertEquals(listOf("Home", "Profile"), items.map { it.contentDescription })
        assertEquals(1, bar.selectedIndex)
        assertEquals(listOf(false, true), items.map { it.isSelected })
        assertEquals(300f, bar.getChildAt(0).x)
    }

    @Test
    fun setTabs_resetsSelectionThatNoLongerExists() {
        val bar = createBar(initialIndex = 2)
        layout(bar)

        bar.setTabs(R.xml.rbb_test_tabs_two)

        assertEquals(0, bar.selectedIndex)
    }

    @Test
    fun setTabs_onEmptyBar_beforeLayout() {
        val bar = ReadableBottomBar(activity, Robolectric.buildAttributeSet().build())
        activity.setContentView(FrameLayout(activity).apply { addView(bar, FrameLayout.LayoutParams(0, 0)) })

        bar.setTabs(R.xml.rbb_test_tabs)
        layout(bar)

        assertEquals(3, bar.findItemViews().size)
    }

    @Test
    fun indicator_usesColorAndCornerRadius() {
        val bar = createBar {
            addAttribute(R.attr.rbb_indicatorColor, "#FF00FF00")
            addAttribute(R.attr.rbb_indicatorCornerRadius, "6px")
        }
        layout(bar)

        val background = bar.getChildAt(0).background as GradientDrawable

        assertEquals(6f, background.cornerRadius)
        assertEquals(Color.GREEN, background.color!!.defaultColor)
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

    private fun createBar(
            initialIndex: Int = 0,
            animationDuration: Long? = null,
            extraAttributes: AttributeSetBuilder.() -> Unit = {}
    ): ReadableBottomBar {
        val attrs = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.rbb_tabs, "@xml/rbb_test_tabs")
                .addAttribute(R.attr.rbb_initialIndex, initialIndex.toString())
                .apply { animationDuration?.let { addAttribute(R.attr.rbb_animationDuration, it.toString()) } }
                .apply(extraAttributes)
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
