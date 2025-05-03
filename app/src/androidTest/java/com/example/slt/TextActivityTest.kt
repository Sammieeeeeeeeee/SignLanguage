package com.example.slt

import android.graphics.drawable.BitmapDrawable
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextActivityTest {

    @Test
    fun testInputText_and_TranslateButton_showsImage() {
        ActivityScenario.launch(TextActivity::class.java)

        // 1. Type "abc" into inputText
        onView(withId(R.id.inputText))
            .perform(typeText("abc"), closeSoftKeyboard())
            .check(matches(withText("abc")))

        // 2. Click translate button
        onView(withId(R.id.translateButton))
            .perform(click())

        // 3. Wait to allow image loading (must match animation delay)
        Thread.sleep(1000)

        // 4. Check if imageView is showing something (non-null drawable)
        onView(withId(R.id.imageView)).check { view, _ ->
            val drawable = (view as? android.widget.ImageView)?.drawable
            assertThat(drawable, instanceOf(BitmapDrawable::class.java))
        }
    }

    @Test
    fun testCloseButton_finishesActivity() {
        val scenario = ActivityScenario.launch(TextActivity::class.java)

        // 1. Click the close button
        onView(withId(R.id.closeBtn)).perform(click())

        // 2. Check if the activity is destroyed
        scenario.onActivity { activity ->
            assertThat(activity.isFinishing, `is`(true))
        }
    }
}
