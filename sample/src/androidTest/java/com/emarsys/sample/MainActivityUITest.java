package com.emarsys.sample;

import android.content.Context;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.Emarsys;
import com.emarsys.core.di.DependencyInjection;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.MobileEngageStatusListener;
import com.emarsys.mobileengage.MobileEngageUtils;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() {
        Context context = InstrumentationRegistry.getTargetContext();
        MobileEngageFragment fragment = ((MobileEngageFragment) ((MainPagerAdapter) mActivityRule.getActivity().viewPager.getAdapter()).getItem(0));

        new MeIdStorage(context).remove();
        new AppLoginStorage(context).remove();

        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .from(MobileEngage.getConfig())
                .enableIdlingResource(true)
                .statusListener((MobileEngageStatusListener) fragment)
                .build();
        MobileEngage.setup(config);

        Espresso.registerIdlingResources(MobileEngageUtils.getIdlingResource());
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        new MeIdStorage(targetContext).remove();
        new AppLoginStorage(targetContext).remove();

        Espresso.unregisterIdlingResources(MobileEngageUtils.getIdlingResource());

        Field handlerField = MobileEngage.class.getDeclaredField("coreSdkHandler");
        handlerField.setAccessible(true);
        Handler handler = (Handler) handlerField.get(null);
        handler.getLooper().quit();
        DependencyInjection.tearDown();
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
        onView(withId(R.id.appLogout)).perform(scrollTo(), click());
        onView(withId(R.id.mobileEngageStatusLabel)).check(matches(withText("Logout: OK")));
    }

    private void login() {
        onView(withId(R.id.contactFieldId)).perform(scrollTo(), typeText("3"));
        onView(withId(R.id.contactFieldValue)).perform(scrollTo(), typeText("test@test.com"));
        onView(withId(R.id.appLogin)).perform(scrollTo(), click());
    }
}
