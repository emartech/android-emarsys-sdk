package com.emarsys.config;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.EventHandler;
import com.emarsys.mobileengage.experimental.FlipperFeature;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.notification.NotificationEventHandler;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class EmarsysConfigTest {
    private String APP_ID = "appID";
    private String SECRET = "5678987654345678654";
    private Application application;
    private OreoConfig mockOreoConfig;
    private EventHandler defaultInAppEventHandler;
    private NotificationEventHandler defaultNotificationEventHandler;
    private FlipperFeature[] features;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Before
    public void init() {
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        mockOreoConfig = mock(OreoConfig.class);
        defaultInAppEventHandler = mock(EventHandler.class);
        defaultNotificationEventHandler = mock(NotificationEventHandler.class);
        features = new FlipperFeature[]{
                mock(FlipperFeature.class),
                mock(FlipperFeature.class)
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationShouldNotBeNull() {
        new EmarsysConfig(
                null,
                APP_ID,
                SECRET,
                true,
                mockOreoConfig,
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCodeShouldNotBeNull() {
        new EmarsysConfig(
                application,
                null,
                SECRET,
                true,
                mockOreoConfig,
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_secretShouldNotBeNull() {
        new EmarsysConfig(
                application,
                APP_ID,
                null,
                true,
                mockOreoConfig,
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigShouldNotBeNull() {
        new EmarsysConfig(
                application,
                APP_ID,
                SECRET,
                true,
                null,
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_featuresShouldNotBeNull() {
        new EmarsysConfig(
                application,
                APP_ID,
                SECRET,
                true,
                mockOreoConfig,
                null,
                null,
                null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigParameter_channelNameShouldNotBeNull_whenEnabled() {
        new EmarsysConfig(
                application,
                APP_ID, SECRET,
                true,
                new OreoConfig(true, null, "description"),
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigParameter_channelDescriptionShouldNotBeNull_whenEnabled() {
        new EmarsysConfig(
                application,
                APP_ID,
                SECRET,
                true,
                new OreoConfig(true, "name", null),
                null,
                null,
                features);
    }

    @Test
    public void testBuilder_withMandatoryArguments() {
        EmarsysConfig expected = new EmarsysConfig(
                application,
                APP_ID,
                SECRET,
                false,
                new OreoConfig(false),
                null,
                null,
                new FlipperFeature[]{});

        EmarsysConfig result = new EmarsysConfig.Builder()
                .application(application)
                .credentials(APP_ID, SECRET)
                .disableDefaultChannel()
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_whenInAppMessagingFlipperIsOn_DefaultInAppMessageHandlerIsRequired() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("DefaultInAppMessageHandler must not be null");

        new EmarsysConfig.Builder()
                .application(application)
                .credentials(APP_ID, SECRET)
                .disableDefaultChannel()
                .enableExperimentalFeatures(MobileEngageFeature.IN_APP_MESSAGING)
                .build();
    }

    @Test
    public void testBuilder_whenInAppMessagingFlipperIsOff_DefaultInAppMessageHandlerIsNotRequired() {
        new EmarsysConfig.Builder()
                .application(application)
                .credentials(APP_ID, SECRET)
                .disableDefaultChannel()
                .build();
    }

    @Test
    public void testBuilder_withAllArguments() {
        EmarsysConfig expected = new EmarsysConfig(
                application,
                APP_ID,
                SECRET,
                true,
                new OreoConfig(true, "defaultChannelName", "defaultChannelDescription"),
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features
        );

        EmarsysConfig result = new EmarsysConfig.Builder()
                .application(application)
                .credentials(APP_ID, SECRET)
                .enableIdlingResource(true)
                .enableDefaultChannel("defaultChannelName", "defaultChannelDescription")
                .enableExperimentalFeatures(features)
                .setDefaultInAppEventHandler(defaultInAppEventHandler)
                .setNotificationEventHandler(defaultNotificationEventHandler)
                .build();

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_from_shouldNotAcceptNull() {
        new EmarsysConfig.Builder().from(null);
    }

    @Test
    public void testBuilder_from() {
        EmarsysConfig expected = new EmarsysConfig(
                application,
                APP_ID,
                SECRET,
                true,
                new OreoConfig(false),
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features);

        EmarsysConfig result = new EmarsysConfig.Builder()
                .from(expected)
                .build();

        assertEquals(expected, result);
    }
}