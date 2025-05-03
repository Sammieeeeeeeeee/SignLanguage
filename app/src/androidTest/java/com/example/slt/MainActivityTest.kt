package com.example.slt

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        // Launch the MainActivity before each test
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }
    @Test
    fun testImageActivity(){
        // Click on the "Image" button
        onView(withId(R.id.CNNmodel)).perform(click())

        // Verify that the Image input interface is displayed
        onView(withId(R.id.main)).check(matches(isDisplayed()))
    }

    @Test
    fun testTextActivity(){
        // Click on the "Text" button
        onView(withId(R.id.textRecognize)).perform(click())

        // Verify that the Image input interface is displayed
        onView(withId(R.id.main)).check(matches(isDisplayed()))
    }

    @Test
    fun testVideoActivity(){
        // Click on the "Image" button
        onView(withId(R.id.DTWmodel)).perform(click())

        // Verify that the Image input interface is displayed
        onView(withId(R.id.main)).check(matches(isDisplayed()))
    }
}