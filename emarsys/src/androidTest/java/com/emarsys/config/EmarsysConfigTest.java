package com.emarsys.config;

import android.app.Application;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.mobileengage.api.EventHandler;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class EmarsysConfigTest {
    private String APP_ID = "appID";
    private String APP_PASSWORD = "5678987654345678654";
    private int CONTACT_FIELD_ID = 567;
    private String MERCHANT_ID = "MERCHANT_ID";
    private Application application;
    private EventHandler defaultInAppEventHandler;
    private NotificationEventHandler defaultNotificationEventHandler;
    private FlipperFeature[] features;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
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
                APP_PASSWORD,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                true,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCodeShouldNotBeNull() {
        new EmarsysConfig(
                application,
                null,
                APP_PASSWORD,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                true,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_secretShouldNotBeNull() {
        new EmarsysConfig(
                application,
                APP_ID,
                null,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                true,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contactFieldIdShouldNotBeNull() {
        new EmarsysConfig(
                application,
                APP_ID,
                APP_PASSWORD,
                null,
                MERCHANT_ID,
                true,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_featuresShouldNotBeNull() {
        new EmarsysConfig(
                application,
                APP_ID,
                APP_PASSWORD,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                true,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_featuresList_shouldNotContainNullElements() {
        new EmarsysConfig(
                application,
                APP_ID,
                APP_PASSWORD,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                true,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                new FlipperFeature[]{mock(FlipperFeature.class), null});
    }

    @Test
    public void testBuilder_withAllArguments() {
        EmarsysConfig expected = new EmarsysConfig(
                application,
                APP_ID,
                APP_PASSWORD,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                true,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features
        );

        EmarsysConfig result = new EmarsysConfig.Builder()
                .application(application)
                .mobileEngageCredentials(APP_ID, APP_PASSWORD)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .enableIdlingResource(true)
                .enableExperimentalFeatures(features)
                .inAppEventHandler(defaultInAppEventHandler)
                .notificationEventHandler(defaultNotificationEventHandler)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_withRequiredArguments() {
        EmarsysConfig expected = new EmarsysConfig(
                application,
                APP_ID,
                APP_PASSWORD,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                false,
                null,
                null,
                new FlipperFeature[]{});

        EmarsysConfig result = new EmarsysConfig.Builder()
                .application(application)
                .mobileEngageCredentials(APP_ID, APP_PASSWORD)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_whenInAppMessagingFlipperIsOff_defaultInAppMessageHandlerIsNotRequired() {
        try {
            new EmarsysConfig.Builder()
                    .application(application)
                    .mobileEngageCredentials(APP_ID, APP_PASSWORD)
                    .contactFieldId(CONTACT_FIELD_ID)
                    .predictMerchantId(MERCHANT_ID)
                    .build();
        } catch (IllegalArgumentException e) {
            fail("Should not fail with: " + e.getMessage());
        }
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
                APP_PASSWORD,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                true,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features);

        EmarsysConfig result = new EmarsysConfig.Builder()
                .from(expected)
                .build();

        assertEquals(expected, result);
    }
}