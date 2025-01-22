package com.iammert.readablebottombar

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.iammert.library.readablebottombar.ReadableBottomBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.root)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val pager = findViewById<ViewPager2>(R.id.pager)
        val bottomBar = findViewById<ReadableBottomBar>(R.id.bottomBar)
        val textBottomBar = findViewById<ReadableBottomBar>(R.id.textBottomBar)

        val titles = listOf(R.string.home, R.string.search, R.string.bag, R.string.favorite, R.string.profile).map(::getString)
        pager.adapter = PageAdapter(titles)

        // ViewPager2 and the bars save their own state, so after recreation
        // they come back in sync without extra work here.
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                listOf(bottomBar, textBottomBar).forEach { bar ->
                    if (bar.selectedIndex != position) bar.selectItem(position)
                }
            }
        })
        val selectPage = ReadableBottomBar.ItemSelectListener { index ->
            if (pager.currentItem != index) pager.currentItem = index
        }
        bottomBar.setOnItemSelectListener(selectPage)
        textBottomBar.setOnItemSelectListener(selectPage)
    }
}
