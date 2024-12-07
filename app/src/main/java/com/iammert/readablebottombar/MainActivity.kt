package com.iammert.readablebottombar

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.iammert.library.readablebottombar.ReadableBottomBar

class MainActivity : AppCompatActivity() {

    private lateinit var pageTitle: TextView
    private lateinit var bottomBar: ReadableBottomBar
    private lateinit var textBottomBar: ReadableBottomBar

    private val pageTitles by lazy {
        listOf(R.string.home, R.string.search, R.string.bag, R.string.favorite, R.string.profile).map(::getString)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.root)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        pageTitle = findViewById(R.id.pageTitle)
        bottomBar = findViewById(R.id.bottomBar)
        textBottomBar = findViewById(R.id.textBottomBar)

        bottomBar.setOnItemSelectListener { index ->
            showPage(index)
            if (textBottomBar.selectedIndex != index) textBottomBar.selectItem(index)
        }
        textBottomBar.setOnItemSelectListener { index ->
            if (bottomBar.selectedIndex != index) bottomBar.selectItem(index)
        }
        showPage(bottomBar.selectedIndex)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // The bars restore their own selection, the page title has to follow.
        showPage(bottomBar.selectedIndex)
    }

    private fun showPage(index: Int) {
        pageTitle.text = getString(R.string.selected_page, pageTitles[index])
    }
}
