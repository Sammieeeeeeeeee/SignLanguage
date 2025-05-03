package com.example.slt

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import org.junit.Assert.assertTrue



@RunWith(AndroidJUnit4::class)
class CNNActivityTest {

    private lateinit var scenario: ActivityScenario<CNNActivity>

    @Before
    fun setup(){
        // Launch the MainActivity before each test
        scenario = ActivityScenario.launch(CNNActivity::class.java)
    }

    @Test
    fun testSelectImageButton(){
        // Initialize Intents
        Intents.init()

        // Perform click on btnSelectImage
        onView(withId(R.id.btnSelectImage)).perform(click())

        // Verify the image picker intent was launched
        Intents.intended(hasAction(Intent.ACTION_GET_CONTENT))

        // Release Intents
        Intents.release()
    }

    @Test
    fun testCloseButton(){
        onView(withId(R.id.closeBtn)).perform(click())
        scenario.onActivity { activity ->
            assertTrue(activity.isFinishing || activity.isDestroyed)
        }
    }
}