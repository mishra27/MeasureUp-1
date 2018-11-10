package com.example.aksha.measureup;

import android.os.Environment;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Before
    public void launchActivity() {
         ActivityScenario.launch(MainActivity.class);
    }

    @Test
    public void testIfDirectoryWrittenAfterRecord() {
        Matcher<View> recordView = withId(R.id.recordButtonView);

        // grab current amount of directories
        File measureUpDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/MeasureUp");
        int files = measureUpDir.list().length;

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
}