package com.example.aksha.measureup;

import android.os.Environment;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.After;
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
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecordScreenFragmentTest {

    public RecordScreenFragmentTest() {
    }

    @Before
    public  void setUp() throws Exception {

    }

    @Test
    public void onCreateView() {
        assertTrue(true);
    }

    @After
    public void tearDown() throws Exception {

    }
}


