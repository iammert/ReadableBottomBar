package com.iammert.library.readablebottombar

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
