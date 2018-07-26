package com.emarsys.mobileengage.config;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.EventHandler;
import com.emarsys.mobileengage.MobileEngageStatusListener;
import com.emarsys.mobileengage.experimental.FlipperFeature;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.notification.NotificationEventHandler;
import com.emarsys.mobileengage.testUtil.ApplicationTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MobileEngageConfigTest {
    private String APP_ID = "appID";
    private String SECRET = "5678987654345678654";
    private MobileEngageStatusListener statusListenerMock;
    private Application application;
    private Application applicationDebug;
    private Application applicationRelease;
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
        applicationDebug = ApplicationTestUtils.applicationDebug();
        applicationRelease = ApplicationTestUtils.applicationRelease();
        statusListenerMock = mock(MobileEngageStatusListener.class);
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
        new MobileEngageConfig(
                null,
                APP_ID,
                SECRET,
                statusListenerMock,
                true,
                false,
                mockOreoConfig,
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCodeShouldNotBeNull() {
        new MobileEngageConfig(
                application,
                null,
                SECRET,
                statusListenerMock,
                true,
                false,
                mockOreoConfig,
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_secretShouldNotBeNull() {
        new MobileEngageConfig(
                application,
                APP_ID,
                null,
                statusListenerMock,
                true,
                false,
                mockOreoConfig,
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigShouldNotBeNull() {
        new MobileEngageConfig(
                application,
                APP_ID,
                SECRET,
                statusListenerMock,
                true,
                false,
                null,
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_featuresShouldNotBeNull() {
        new MobileEngageConfig(
                application,
                APP_ID,
                SECRET,
                statusListenerMock,
                true,
                false,
                mockOreoConfig,
                null,
                null,
                null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigParameter_channelNameShouldNotBeNull_whenEnabled() {
        new MobileEngageConfig(
                application,
                APP_ID, SECRET,
                statusListenerMock,
                true,
                false,
                new OreoConfig(true, null, "description"),
                null,
                null,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigParameter_channelDescriptionShouldNotBeNull_whenEnabled() {
        new MobileEngageConfig(
                application,
                APP_ID,
                SECRET,
                statusListenerMock,
                true,
                false,
                new OreoConfig(true, "name", null),
                null,
                null,
                features);
    }

    @Test
    public void testBuilder_withCorrectSetup() {
        MobileEngageConfig expected = new MobileEngageConfig(
                applicationDebug,
                APP_ID,
                SECRET,
                null,
                true,
                false,
                new OreoConfig(false),
                null,
                null,
                new FlipperFeature[]{});

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials(APP_ID, SECRET)
                .disableDefaultChannel()
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_whenInAppMessagingFlipperIsOn_DefaultInAppMessageHandlerIsRequired() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("DefaultInAppMessageHandler must not be null");

        new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials(APP_ID, SECRET)
                .disableDefaultChannel()
                .enableExperimentalFeatures(MobileEngageFeature.IN_APP_MESSAGING)
                .build();
    }

    @Test
    public void testBuilder_whenInAppMessagingFlipperIsOff_DefaultInAppMessageHandlerIsNotRequired() {
        new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials(APP_ID, SECRET)
                .disableDefaultChannel()
                .build();
    }

    @Test
    public void testBuilder_withAllArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(
                applicationDebug,
                APP_ID,
                SECRET,
                statusListenerMock,
                true,
                true,
                new OreoConfig(true, "defaultChannelName", "defaultChannelDescription"),
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features
        );

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials(APP_ID, SECRET)
                .statusListener(statusListenerMock)
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
        new MobileEngageConfig.Builder().from(null);
    }

    @Test
    public void testBuilder_from() {
        MobileEngageConfig expected = new MobileEngageConfig(
                applicationDebug,
                APP_ID,
                SECRET,
                statusListenerMock,
                true,
                true,
                new OreoConfig(false),
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .from(expected)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_withDebugApplication() {
        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials("", "")
                .disableDefaultChannel()
                .build();
        assertTrue(result.isDebugMode());
    }

    @Test
    public void testBuilder_withReleaseApplication() {
        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationRelease)
                .credentials("", "")
                .disableDefaultChannel()
                .build();
        assertFalse(result.isDebugMode());
    }
}