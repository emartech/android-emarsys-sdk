package com.emarsys.config;

import android.app.Application;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.mobileengage.api.NotificationEventHandler;
import com.emarsys.mobileengage.api.event.EventHandler;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public class EmarsysConfigTest {
    private String APP_ID = "appID";
    private int CONTACT_FIELD_ID = 567;
    private String MERCHANT_ID = "MERCHANT_ID";
    private Application application;
    private EventHandler defaultInAppEventHandler;
    private EventHandler defaultNotificationEventHandler;
    private FlipperFeature[] features;
    private boolean automaticPushTokenSending;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        automaticPushTokenSending = true;
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        defaultInAppEventHandler = mock(EventHandler.class);
        defaultNotificationEventHandler = mock(EventHandler.class);
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
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features,
                automaticPushTokenSending);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contactFieldIdShouldNotBeNull() {
        new EmarsysConfig(
                application,
                APP_ID,
                null,
                MERCHANT_ID,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features,
                automaticPushTokenSending);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_featuresShouldNotBeNull() {
        new EmarsysConfig(
                application,
                APP_ID,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                null,
                automaticPushTokenSending);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_featuresList_shouldNotContainNullElements() {
        new EmarsysConfig(
                application,
                APP_ID,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                new FlipperFeature[]{mock(FlipperFeature.class), null},
                automaticPushTokenSending);
    }

    @Test
    public void testBuilder_withAllArguments() {
        EmarsysConfig expected = new EmarsysConfig(
                application,
                APP_ID,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features,
                automaticPushTokenSending);

        EmarsysConfig result = new EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .enableExperimentalFeatures(features)
                .inAppEventHandler(any(com.emarsys.mobileengage.api.EventHandler.class))
                .notificationEventHandler(any(NotificationEventHandler.class))
                .build();

        assertEquals(expected.getApplication(), result.getApplication());
        assertEquals(expected.getContactFieldId(), result.getContactFieldId());
        assertEquals(expected.getExperimentalFeatures(), result.getExperimentalFeatures());
        assertEquals(expected.getMobileEngageApplicationCode(), result.getMobileEngageApplicationCode());
        assertEquals(expected.getPredictMerchantId(), result.getPredictMerchantId());
        Assert.assertTrue(result.getInAppEventHandler().getClass().isInstance(expected.getInAppEventHandler()));
        Assert.assertTrue(result.getNotificationEventHandler().getClass().isInstance(expected.getNotificationEventHandler()));
    }

    @Test
    public void testBuilder_withRequiredArguments() {
        EmarsysConfig expected = new EmarsysConfig(
                application,
                APP_ID,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                null,
                null,
                new FlipperFeature[]{},
                automaticPushTokenSending);

        EmarsysConfig result = new EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
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
                    .mobileEngageApplicationCode(APP_ID)
                    .contactFieldId(CONTACT_FIELD_ID)
                    .predictMerchantId(MERCHANT_ID)
                    .build();
        } catch (IllegalArgumentException e) {
            fail("Should not fail with: " + e.getMessage());
        }
    }

    @Test
    public void testBuilder_automaticPushTokenSending_whenDisabled() {
        EmarsysConfig config = new EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .disableAutomaticPushTokenSending()
                .build();
        assertFalse(config.isAutomaticPushTokenSendingEnabled());
    }

    @Test
    public void testBuilder_automaticPushTokenSending_default() {
        EmarsysConfig config = new EmarsysConfig.Builder()
                .application(application)
                .mobileEngageApplicationCode(APP_ID)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .build();
        assertTrue(config.isAutomaticPushTokenSendingEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void
    testBuilder_from_shouldNotAcceptNull() {
        new EmarsysConfig.Builder().from(null);
    }

    @Test
    public void testBuilder_from() {
        EmarsysConfig expected = new EmarsysConfig(
                application,
                APP_ID,
                CONTACT_FIELD_ID,
                MERCHANT_ID,
                defaultInAppEventHandler,
                defaultNotificationEventHandler,
                features,
                automaticPushTokenSending);

        EmarsysConfig result = new EmarsysConfig.Builder()
                .from(expected)
                .build();

        assertEquals(expected.getApplication(), result.getApplication());
        assertEquals(expected.getContactFieldId(), result.getContactFieldId());
        assertEquals(expected.getExperimentalFeatures(), result.getExperimentalFeatures());
        assertEquals(expected.getMobileEngageApplicationCode(), result.getMobileEngageApplicationCode());
        assertEquals(expected.getPredictMerchantId(), result.getPredictMerchantId());
        Assert.assertTrue(result.getInAppEventHandler().getClass().isInstance(expected.getInAppEventHandler()));
        Assert.assertTrue(result.getNotificationEventHandler().getClass().isInstance(expected.getNotificationEventHandler()));
    }
}