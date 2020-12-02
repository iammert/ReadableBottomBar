package com.iammert.readablebottombar

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.iammert.library.readablebottombar.BottomBarItem
import com.iammert.library.readablebottombar.ReadableBottomBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomBar : ReadableBottomBar = findViewById<ReadableBottomBar>(R.id.dynamic_bottom_bar)
        //bottomBar.setTabItems(R.xml.tabs_disabled)
        var items =  ArrayList<BottomBarItem>()

        items.add(BottomBarItem(0, "Test", 15f,
                Color.BLACK, Color.BLACK, getDrawable(R.drawable.ic_home_black_24dp),
                ReadableBottomBar.ItemType.Icon))
        bottomBar.setTabItems(items)
    }
}
