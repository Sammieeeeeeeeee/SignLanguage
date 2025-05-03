package com.example.slt

import android.app.Activity.RESULT_OK
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.junit.After


@RunWith(AndroidJUnit4::class)
class DTWActivityTest {

    private lateinit var scenario: ActivityScenario<DTWActivity>

    @Before
    fun setup(){
        // Launch the MainActivity before each test
        scenario = ActivityScenario.launch(DTWActivity::class.java)
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test_btnSelectImage_launchesVideoPicker() {
        // Launch the activity
        ActivityScenario.launch(DTWActivity::class.java)

        // Stub out the response to avoid real gallery launch
        val resultData = Intent()
        val result = Instrumentation.ActivityResult(RESULT_OK, resultData)
        Intents.intending(hasAction(Intent.ACTION_PICK)).respondWith(result)

        // Perform click on the button
        onView(withId(R.id.btnSelectImage)).perform(click())

        // Check that the correct intent was sent
        Intents.intended(allOf(
            hasAction(Intent.ACTION_CHOOSER),
            hasExtra(`is`(Intent.EXTRA_INTENT), allOf(
                hasAction(Intent.ACTION_PICK),
                hasType("video/*")
            ))
        ))
    }

    @Test
    fun testCloseButton(){
        onView(withId(R.id.closeBtn)).perform(click())
        scenario.onActivity { activity ->
            assertTrue(activity.isFinishing || activity.isDestroyed)
        }
    }
}