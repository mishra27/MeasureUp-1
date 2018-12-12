package com.example.aksha.measureup;


import android.os.Environment;
import android.view.View;


import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;


import java.io.File;

import androidx.test.core.app.ActivityScenario;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertTrue;


public class MainActivityTest {

    @Before
    public void launchActivity() {
        ActivityScenario.launch(MainActivity.class);
    }

    public void goToRecordScreen(){

        // wait for the app to load
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Matcher<View> enterPassword = withId(R.id.editText3);
        onView(enterPassword).perform(typeText("1234"), closeSoftKeyboard());

        onView(withText("ENTER")).perform(click());

        // wait for feature points to appear
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // Checking whether contents are getting saved in the phone storage
    @Test
    public void testingPhoneStorage() {

        this.goToRecordScreen();

        Matcher<View> recordView = withId(R.id.recordButtonView);

        // grab current amount of directories
        File measureUpDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/.MeasureUp");

        int files = 0;
        if (measureUpDir.exists()) {
            if (measureUpDir.list() != null) {
                files = measureUpDir.list().length;
            }
        }

        // click record button (start recording)
        onView(recordView).perform(click());

        // wait for app to record for 1s
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // stop recording
        onView(recordView).perform(click());

        // wait for files to be written
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(measureUpDir.list().length > files);
    }

    // Checking the correctness of logIn page after the pin has been setup
    @Test
    public void testingLogInPage(){

        // wait for the app to load
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Matcher<View> enterPassword = withId(R.id.editText3);
        onView(enterPassword).check(matches(isDisplayed()));

        // Checking LogIn buttons
        onView(withText("ENTER")).check(matches(isDisplayed()));
        onView(withText("ENTER")).check(matches(isClickable()));
        onView(withText("FORGOT?")).check(matches(isDisplayed()));
        onView(withText("FORGOT?")).check(matches(isClickable()));

        onView(withText("FORGOT?")).perform(click());

        // wait for next page to load
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("Home Town?")).check(matches(isDisplayed()));
        onView(withText("ENTER")).check(matches(isDisplayed()));
        onView(withText("ENTER")).check(matches(isClickable()));

    }

    @Test
    public void testingSettingsButton() {

        this.goToRecordScreen();

        Matcher<View> settings = withId(R.id.imageButton);
        onView(settings).perform(click());

        // wait for next page to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Matcher<View> password = withText("Password");
        Matcher<View> securityQ = withText("Security Question");

        // Checking the pwd and sQ buttons
        onView(password).check(matches(isDisplayed()));
        onView(securityQ).check(matches(isDisplayed()));
        onView(password).check(matches(isClickable()));
        onView(securityQ).check(matches(isClickable()));

        onView(password).perform(click());

        // wait for next page to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Matcher<View> disablePassword = withText("Disable Password");
        Matcher<View> changePassword = withText("Change Password");

        // Checking the password buttons
        onView(changePassword).check(matches(isDisplayed()));
        onView(changePassword).check(matches(isClickable()));
        onView(disablePassword).check(matches(isDisplayed()));
        onView(disablePassword).check(matches(isClickable()));

        onView(disablePassword).perform(click());

        // wait for next page to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Matcher<View> cancel = withText("CANCEL");
        Matcher<View> ok = withText("OK");

        // Checking the disablePassword dialogue
        onView(cancel).check(matches(isDisplayed()));
        onView(cancel).check(matches(isClickable()));
        onView(ok).check(matches(isDisplayed()));
        onView(ok).check(matches(isClickable()));

        onView(withText("Verify Password")).check(matches(isDisplayed()));

        onView(cancel).perform(click());

        // wait for next page to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(changePassword).perform(click());

        // wait for next page to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Matcher<View> confirm = withText("CONFIRM");
        onView(confirm).check(matches(isDisplayed()));
        onView(confirm).check(matches(isClickable()));

        onView(withText("Change Password")).check(matches(isDisplayed()));
        onView(withId(R.id.editText4)).check(matches(isDisplayed()));
        onView(withId(R.id.editText10)).check(matches(isDisplayed()));
        onView(withId(R.id.editText11)).check(matches(isDisplayed()));

    }

    // Checking for dialogue box, buttons and the reference distance
    @Test
    public void testingReferenceDistance(){

        this.goToRecordScreen();

        // Checking the settings and gallery button
        onView(withId(R.id.imageButton)).check(matches(isClickable()));
        onView(withId(R.id.imageButton)).check(matches(isDisplayed()));
        onView(withId(R.id.imageButton2)).check(matches(isClickable()));
        onView(withId(R.id.imageButton2)).check(matches(isDisplayed()));

        Matcher<View> recordView = withId(R.id.recordButtonView);
        // click record button (start recording)
        onView(recordView).perform(click());

        // wait for app to record for 5s
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // stop recording
        onView(recordView).perform(click());

        // wait for dialogue box to appear
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Checking the Save button
        Matcher<View> saveView = withText("Save");
        onView(saveView).check(matches(isClickable()));
        onView(saveView).check(matches(isDisplayed()));

        // Checking the discard button
        Matcher<View> discardView = withText("Discard");
        onView(discardView).check(matches(isClickable()));
        onView(discardView).check(matches(isDisplayed()));

        onView(discardView).perform(click());

        // Checking whether reference distance is being displayed or not
        onView(withId(R.id.textView)).check(matches(not(withText(""))));

    }

    // Checking whether the results are being displayed or not (This is right after saving the object by clicking "yes")
    @Test
    public void testingAlgorithm() {

        this.goToRecordScreen();

        Matcher<View> recordView = withId(R.id.recordButtonView);

        // click record button (start recording)
        onView(recordView).perform(click());

        // wait for app to record for 10s
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // stop recording
        onView(recordView).perform(click());

        // wait for dialogue box to appear
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withHint("Enter a title for the object")).perform(typeText("randomObject"), closeSoftKeyboard());

        // Checking the Save button
        Matcher<View> saveView = withText("Save");
        onView(saveView).perform(click());

        // wait for second dialogue box to appear
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("Your object has been saved in the gallery!\nWould you like to measure it?")).check(matches(isDisplayed()));

        // Checking the Yes button
        Matcher<View> yesView = withText("Yes");
        onView(yesView).check(matches(isClickable()));
        onView(yesView).check(matches(isDisplayed()));

        // Checking the No button
        Matcher<View> noView = withText("No");
        onView(noView).check(matches(isClickable()));
        onView(noView).check(matches(isDisplayed()));

        onView(yesView).perform(click());

        // wait for pointers to appear
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Checking the measure button
        Matcher<View> measureView = withText("MEASURE");
        onView(measureView).check(matches(isClickable()));
        onView(measureView).check(matches(isDisplayed()));

        onView(measureView).perform(click());

        // waiting for result to appear
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("Results")).check(matches(isDisplayed()));
    }

    // Checking whether the object is getting saved/deleted or not
    @Test
    public void testingObjectDatabase() {

        this.goToRecordScreen();

        Matcher<View> recordView = withId(R.id.recordButtonView);

        // click record button (start recording)
        onView(recordView).perform(click());

        // wait for app to record for 5s
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // stop recording
        onView(recordView).perform(click());

        // wait for dialogue box to appear
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withHint("Enter a title for the object")).perform(typeText("firstObject"), closeSoftKeyboard());

        Matcher<View> saveView = withText("Save");
        onView(saveView).perform(click());

        // wait for second dialogue box to appear
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Matcher<View> noView = withText("No");
        onView(noView).perform(click());

        onView(withId(R.id.imageButton2)).perform(click());

        // wait for prev page to appear
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Checking if object is in the database
        onView(withText("firstObject")).check(matches(isDisplayed()));

        onView(withText("firstObject")).perform(click());

        // wait for next page to appear
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("New Measurement")).check(matches(isDisplayed()));
        onView(withText("New Measurement")).check(matches(isClickable()));
        onView(withText("Measurements")).check(matches(isDisplayed()));
        onView(withText("Name")).check(matches(isDisplayed()));
        onView(withText("Distance")).check(matches(isDisplayed()));

        Matcher<View> delete = withId(R.id.imageButton3);
        onView(delete).check(matches(isClickable()));
        onView(delete).check(matches(isDisplayed()));

        onView(delete).perform(click());

        // wait for prev page to appear
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("firstObject")).check(doesNotExist());
    }
}
