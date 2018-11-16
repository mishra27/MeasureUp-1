package com.example.aksha.measureup;

import android.view.View;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import androidx.test.core.app.ActivityScenario;

import static org.junit.Assert.*;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;


public class MainActivityTest {

    @Before
    public void launchActivity() {
        ActivityScenario.launch(MainActivity.class);
    }


    @Test
    public void testSettingsDisplay() {

        onView(withId(R.id.imageButton)).check(matches(isClickable()));
        onView(withId(R.id.imageButton)).check(matches(isDisplayed()));
        Matcher<View> settings = withId(R.id.imageButton);

        onView(settings).perform(click());
        onView(withText("Gallery")).check(matches(isDisplayed()));
        onView(withText("Password")).check(matches(isDisplayed()));
        onView(withText("Security Question")).check(matches(isDisplayed()));

        onView(withId(R.id.button2)).check(matches(isClickable()));
        onView(withId(R.id.button2)).check(matches(isDisplayed()));
        Matcher<View> password = withId(R.id.button2);
        onView(password).perform(click());


        onView(withId(R.id.button10)).check(matches(isClickable()));
        onView(withId(R.id.button10)).check(matches(isDisplayed()));
        Matcher<View> securityQuestion = withId(R.id.button2);
        onView(securityQuestion).perform(click());
    }
    @Test
    public void testGalleryDisplay() {

        onView(withId(R.id.imageButton2)).check(matches(isClickable()));
        onView(withId(R.id.imageButton2)).check(matches(isDisplayed()));
        Matcher<View> gallery = withId(R.id.imageButton2);

        onView(gallery).perform(click());
        onView(withText("Gallery")).check(matches(isDisplayed()));

    }
  /*  @Test
    public void navigationChecker() {


        Matcher<View> settings = withId(R.id.imageButton);
        onView(settings).perform(click());

    }

    @Test
    public void onSupportNavigateUp() {
        assertTrue(true);
    }
*/
}
