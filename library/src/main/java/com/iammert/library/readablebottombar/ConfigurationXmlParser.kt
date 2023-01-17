package com.iammert.library.readablebottombar

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import androidx.annotation.XmlRes
import androidx.core.content.ContextCompat
import org.xmlpull.v1.XmlPullParserException

class ConfigurationXmlParser(private val context: Context, @XmlRes private val xmlRes: Int) {

    fun parse(): ArrayList<BottomBarItemConfig> {
        val itemConfigList: ArrayList<BottomBarItemConfig> = arrayListOf()
        context.resources.getXml(xmlRes).use { parser ->
            try {
                var eventType: Int
                do {
                    eventType = parser.next()
                    if (eventType == XmlResourceParser.START_TAG && KEY_TAB == parser.name) {
                        itemConfigList.add(getTabConfig(parser, itemConfigList.size))
                    }
                } while (eventType != XmlResourceParser.END_DOCUMENT)
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                throw Exception()
            }
        }
        return itemConfigList
    }

    private fun getTabConfig(parser: XmlResourceParser, index: Int): BottomBarItemConfig {
        val attributeCount = parser.attributeCount
        var itemText: String? = null
        var itemDrawable: Drawable? = null
        for (i in 0 until attributeCount) {
            when (parser.getAttributeName(i)) {
                KEY_TEXT -> itemText = getText(parser, i)
                KEY_DRAWABLE -> itemDrawable = getDrawable(parser, i)
            }
        }
        return BottomBarItemConfig(text = itemText!!, drawable = itemDrawable!!, index = index)
    }

    private fun getDrawable(parser: XmlResourceParser, i: Int): Drawable {
        return ContextCompat.getDrawable(context, parser.getAttributeResourceValue(i, 0))!!
    }

    private fun getText(parser: XmlResourceParser, i: Int): String {
        return context.getString(parser.getAttributeResourceValue(i, 0))
    }

    companion object {
        const val KEY_TEXT: String = "text"
        const val KEY_DRAWABLE: String = "drawable"

        const val KEY_TAB: String = "tab"
    }

}
