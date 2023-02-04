package com.iammert.library.readablebottombar

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigurationXmlParserTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun parse_readsTabsInDeclarationOrder() {
        val configs = ConfigurationXmlParser(context, R.xml.rbb_test_tabs).parse()

        assertEquals(listOf("Home", "Search", "Profile"), configs.map { it.text })
        assertEquals(listOf(0, 1, 2), configs.map { it.index })
        configs.forEach { assertNotNull(it.drawable) }
    }

    @Test
    fun parse_calledTwice_returnsTheSameTabs() {
        val parser = ConfigurationXmlParser(context, R.xml.rbb_test_tabs)
        val first = parser.parse().map { it.text }

        assertEquals(first, parser.parse().map { it.text })
    }

    @Test
    fun parse_missingText_reportsTabPositionAndAttribute() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            ConfigurationXmlParser(context, R.xml.rbb_test_tabs_missing_text).parse()
        }

        assertTrue(error.message!!.contains("position 1"))
        assertTrue(error.message!!.contains("'text'"))
        assertTrue(error.message!!.contains("rbb_test_tabs_missing_text"))
    }

    @Test
    fun parse_literalText_isUsedAsIs() {
        val configs = ConfigurationXmlParser(context, R.xml.rbb_test_tabs_literal_text).parse()

        assertEquals("Feed", configs.single().text)
    }
}
