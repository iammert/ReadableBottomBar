package com.iammert.readablebottombar

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import com.iammert.library.readablebottombar.ReadableBottomBar
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun tappingTab_showsPageAndSyncsOtherBar() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(allOf(withContentDescription("Bag"), isDescendantOfA(withId(R.id.bottomBar)))).perform(click())

            scenario.onActivity { activity ->
                assertEquals(2, activity.findViewById<ViewPager2>(R.id.pager).currentItem)
                assertEquals(2, activity.findViewById<ReadableBottomBar>(R.id.textBottomBar).selectedIndex)
            }
        }
    }

    @Test
    fun swipingPages_updatesBothBars() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.pager)).perform(swipeLeft())

            scenario.onActivity { activity ->
                assertEquals(1, activity.findViewById<ReadableBottomBar>(R.id.bottomBar).selectedIndex)
                assertEquals(1, activity.findViewById<ReadableBottomBar>(R.id.textBottomBar).selectedIndex)
            }
        }
    }

    @Test
    fun selection_survivesRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(allOf(withContentDescription("Profile"), isDescendantOfA(withId(R.id.textBottomBar)))).perform(click())

            scenario.recreate()

            scenario.onActivity { activity ->
                assertEquals(4, activity.findViewById<ViewPager2>(R.id.pager).currentItem)
                assertEquals(4, activity.findViewById<ReadableBottomBar>(R.id.bottomBar).selectedIndex)
                assertEquals(4, activity.findViewById<ReadableBottomBar>(R.id.textBottomBar).selectedIndex)
            }
        }
    }
}
