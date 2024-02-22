package com.iammert.library.readablebottombar

import android.content.Context
import android.os.Looper
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class BottomBarItemViewTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun select_beforeLayout_showsContentWithoutCrashing() {
        val item = createItem()

        item.select()

        assertEquals(View.VISIBLE, item.textView().visibility)
        assertEquals(0f, item.textView().translationY)
    }

    @Test
    fun deselect_hidesContentOnceAnimationEnds() {
        val item = createItem()
        layout(item)
        item.select(animate = false)

        item.deselect()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ReadableBottomBar.ANIMATION_DURATION))

        assertEquals(View.INVISIBLE, item.textView().visibility)
    }

    @Test
    fun selectWhileDeselecting_keepsContentVisible() {
        val item = createItem()
        layout(item)
        item.select(animate = false)

        item.deselect()
        item.select()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ReadableBottomBar.ANIMATION_DURATION))

        assertEquals(View.VISIBLE, item.textView().visibility)
        assertEquals(0f, item.textView().translationY)
    }

    @Test
    fun item_showsTouchFeedback() {
        val item = createItem()

        assertNotNull(item.foreground)
    }

    private fun createItem() = BottomBarItemView(context).apply {
        setText("Home")
        setItemType(ReadableBottomBar.ItemType.Text)
    }

    private fun layout(item: View) {
        item.measure(
                View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY)
        )
        item.layout(0, 0, 200, 100)
    }

    private fun BottomBarItemView.textView(): View = findViewById(R.id.textView)
}
