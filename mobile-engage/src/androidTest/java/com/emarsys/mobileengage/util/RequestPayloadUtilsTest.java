package com.emarsys.mobileengage.util;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.iam.model.IamConversionUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.RandomTestUtils;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestPayloadUtilsTest {
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String APPLICATION_PASSWORD = "applicationPassword";
    public static final String MOBILEENGAGE_SDK_VERSION = BuildConfig.VERSION_NAME;
    public static final String PUSH_TOKEN = "pushToken";
    public static final String REQUEST_ID = "REQUEST_ID";

    private RequestContext requestContext;
    private RequestIdProvider requestIdProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application((Application) InstrumentationRegistry.getTargetContext().getApplicationContext())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();

        requestIdProvider = mock(RequestIdProvider.class);
        when(requestIdProvider.provideId()).thenReturn(REQUEST_ID);

        requestContext = new RequestContext(
                config,
                new DeviceInfo(InstrumentationRegistry.getContext()),
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class),
                requestIdProvider);

        requestContext.setAppLoginParameters(new AppLoginParameters(3, "test@test.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_requestContext_ShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(null);
    }

    @Test
    public void testCreateBasePayload_shouldReturnTheCorrectPayload() {
        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);
        Map<String, Object> expected = RequestPayloadUtils.createBasePayload(new HashMap<String, Object>(), requestContext);
        assertEquals(expected, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_additionalPayloadShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(null, requestContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_requestContext_ShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(new HashMap<String, Object>(), null);
    }

    @Test
    public void testCreateBasePayload_map_shouldReturnTheCorrectMap() {
        requestContext.setAppLoginParameters(new AppLoginParameters());
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", requestContext.getApplicationCode());
        expected.put("hardware_id", requestContext.getDeviceInfo().getHwid());
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        Map<String, Object> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(input, requestContext);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_appLoginParameters_hasCredentials() {
        int contactFieldId = 123;
        String contactFieldValue = "contactFieldValue";

        requestContext.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));

        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", requestContext.getApplicationCode());
        expected.put("hardware_id", requestContext.getDeviceInfo().getHwid());
        expected.put("contact_field_id", contactFieldId);
        expected.put("contact_field_value", contactFieldValue);

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(requestContext);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_appLoginParameters_withoutCredentials() {
        requestContext.setAppLoginParameters(new AppLoginParameters());

        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", requestContext.getApplicationCode());
        expected.put("hardware_id", requestContext.getDeviceInfo().getHwid());

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(requestContext);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_whenAppLoginParameters_isNull() {
        requestContext.setAppLoginParameters(new AppLoginParameters());
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", requestContext.getApplicationCode());
        expected.put("hardware_id", requestContext.getDeviceInfo().getHwid());

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(requestContext);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAppLoginPayload_requestContext_mustNotBeNull() {
        RequestPayloadUtils.createAppLoginPayload(null, PUSH_TOKEN);
    }

    @Test
    public void testCreateAppLoginPayload_withMissingPushToken() {
        Map<String, Object> expected = RequestPayloadUtils.createBasePayload(requestContext);
        expected.put("platform", requestContext.getDeviceInfo().getPlatform());
        expected.put("language", requestContext.getDeviceInfo().getLanguage());
        expected.put("timezone", requestContext.getDeviceInfo().getTimezone());
        expected.put("device_model", requestContext.getDeviceInfo().getModel());
        expected.put("application_version", requestContext.getDeviceInfo().getApplicationVersion());
        expected.put("os_version", requestContext.getDeviceInfo().getOsVersion());
        expected.put("ems_sdk", MOBILEENGAGE_SDK_VERSION);

        expected.put("push_token", false);

        Map<String, Object> result = RequestPayloadUtils.createAppLoginPayload(requestContext, null);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateAppLoginPayload_withPushToken() {
        Map<String, Object> expected = RequestPayloadUtils.createBasePayload(requestContext);
        expected.put("platform", requestContext.getDeviceInfo().getPlatform());
        expected.put("language", requestContext.getDeviceInfo().getLanguage());
        expected.put("timezone", requestContext.getDeviceInfo().getTimezone());
        expected.put("device_model", requestContext.getDeviceInfo().getModel());
        expected.put("application_version", requestContext.getDeviceInfo().getApplicationVersion());
        expected.put("os_version", requestContext.getDeviceInfo().getOsVersion());
        expected.put("ems_sdk", MOBILEENGAGE_SDK_VERSION);

        expected.put("push_token", PUSH_TOKEN);

        Map<String, Object> result = RequestPayloadUtils.createAppLoginPayload(requestContext, PUSH_TOKEN);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_eventsMustNotBeNull() {
        RequestPayloadUtils.createCompositeRequestModelPayload(
                null,
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                requestContext.getDeviceInfo(),
                false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_displayedIamsMustNotBeNull() {
        RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                null,
                Collections.<ButtonClicked>emptyList(),
                requestContext.getDeviceInfo(),
                false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_buttonClicksMustNotBeNull() {
        RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                Collections.<DisplayedIam>emptyList(),
                null,
                requestContext.getDeviceInfo(),
                false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_deviceInfoMustNotBeNull() {
        RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                null,
                false);
    }

    @Test
    public void testCreateCompositeRequestModelPayload_payloadContainsDoNotDisturb_whenDoNotDisturbIsTrue() {
        Map<String, Object> payload = RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                requestContext.getDeviceInfo(),
                true);

        assertTrue((Boolean) payload.get("dnd"));
    }

    @Test
    public void testCreateCompositeRequestModelPayload() {
        List<Object> events = Arrays.asList(
                RandomTestUtils.randomMap(),
                RandomTestUtils.randomMap(),
                RandomTestUtils.randomMap()
        );
        List<DisplayedIam> displayedIams = Arrays.asList(
                RandomTestUtils.randomDisplayedIam(),
                RandomTestUtils.randomDisplayedIam()
        );
        List<ButtonClicked> buttonClicks = Arrays.asList(
                RandomTestUtils.randomButtonClick(),
                RandomTestUtils.randomButtonClick(),
                RandomTestUtils.randomButtonClick()
        );
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("events", events);
        expectedPayload.put("viewed_messages", IamConversionUtils.displayedIamsToArray(displayedIams));
        expectedPayload.put("clicks", IamConversionUtils.buttonClicksToArray(buttonClicks));
        expectedPayload.put("hardware_id", requestContext.getDeviceInfo().getHwid());
        expectedPayload.put("language", requestContext.getDeviceInfo().getLanguage());
        expectedPayload.put("application_version", requestContext.getDeviceInfo().getApplicationVersion());
        expectedPayload.put("ems_sdk", MobileEngageInternal.MOBILEENGAGE_SDK_VERSION);

        Map<String, Object> resultPayload = RequestPayloadUtils.createCompositeRequestModelPayload(
                events,
                displayedIams,
                buttonClicks,
                requestContext.getDeviceInfo(),
                false);

        assertEquals(expectedPayload, resultPayload);
    }

}