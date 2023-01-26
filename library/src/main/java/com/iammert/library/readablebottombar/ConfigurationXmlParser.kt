package com.iammert.library.readablebottombar

import android.content.Context
import android.content.res.Resources
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
                throw IllegalArgumentException("Tab configuration ${resourceName()} is not valid XML", e)
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
        return BottomBarItemConfig(
                text = itemText ?: throw missingAttribute(KEY_TEXT, index),
                drawable = itemDrawable ?: throw missingAttribute(KEY_DRAWABLE, index),
                index = index
        )
    }

    private fun missingAttribute(name: String, index: Int): IllegalArgumentException {
        return IllegalArgumentException("Tab at position $index in ${resourceName()} is missing the '$name' attribute")
    }

    private fun resourceName(): String {
        return try {
            context.resources.getResourceEntryName(xmlRes)
        } catch (e: Resources.NotFoundException) {
            "#0x${Integer.toHexString(xmlRes)}"
        }
    }

    private fun getDrawable(parser: XmlResourceParser, i: Int): Drawable {
        val resId = parser.getAttributeResourceValue(i, 0)
        return ContextCompat.getDrawable(context, resId)
                ?: throw IllegalArgumentException("Drawable #0x${Integer.toHexString(resId)} could not be loaded")
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
