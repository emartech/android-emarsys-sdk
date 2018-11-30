package com.emarsys.sample;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.emarsys.sample.testutils.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() {
        logout();
    }

    @After
    public void tearDown() {
        logout();
    }

    @Test
    public void testAnonymousLogin() {
        onView(ViewMatchers.withId(R.id.appLoginAnonymous)).perform(scrollTo(), click());
        onView(withId(R.id.mobileEngageStatusLabel)).check(matches(withText("Anonymous login: OK")));
    }

    @Test
    public void testLogin() {
        login();
        onView(withId(R.id.mobileEngageStatusLabel)).check(matches(withText("Login: OK")));
    }

    @Test
    public void testCustomEvent_noAttributes() {
        login();
        onView(withId(R.id.mobileEngageStatusLabel)).check(matches(withText("Login: OK")));
        onView(withId(R.id.eventName)).perform(scrollTo(), typeText("eventName"));
        onView(withId(R.id.customEvent)).perform(scrollTo(), click());
        onView(withId(R.id.mobileEngageStatusLabel)).check(matches(withText("Custom event: OK")));
    }

    @Test
    public void testCustomEvent_withAttributes() {
        login();
        onView(withId(R.id.eventName)).perform(scrollTo(), typeText("eventName"));
        onView(withId(R.id.eventAttributes)).perform(scrollTo(), typeText("{attr1: true, attr2: 34, attr3: \"customString\"}"));
        onView(withId(R.id.customEvent)).perform(scrollTo(), click());
        onView(withId(R.id.mobileEngageStatusLabel)).check(matches(withText("Custom event: OK")));
    }

    @Test
    public void testMessageOpen() {
        login();
        onView(withId(R.id.messageId)).perform(scrollTo(), typeText("dd8_zXfDdndBNEQi"));
        onView(withId(R.id.messageOpen)).perform(scrollTo(), click());
        onView(withId(R.id.mobileEngageStatusLabel)).check(matches(withText("Message open: OK")));
    }

    @Test
    public void testLogout() {
        logout();
        onView(withId(R.id.mobileEngageStatusLabel)).check(matches(withText("Logout: OK")));
    }

    private void login() {
        onView(withId(R.id.contactFieldValue)).perform(scrollTo(), typeText("test@test.com"));
        onView(withId(R.id.appLogin)).perform(scrollTo(), click());
    }

    private void logout() {
        onView(withId(R.id.appLogout)).perform(scrollTo(), click());
    }
}
