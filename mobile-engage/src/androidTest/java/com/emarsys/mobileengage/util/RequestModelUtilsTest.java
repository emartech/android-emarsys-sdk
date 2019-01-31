package com.emarsys.mobileengage.util;

import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.device.LanguageProvider;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.RequestModelTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.SharedPrefsUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestModelUtilsTest {
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String APPLICATION_PASSWORD = "applicationPassword";
    public static final String VALID_CUSTOM_EVENT_V3 = "https://mobile-events.eservice.emarsys.net/v3/devices/12345/events";
    public static final String REQUEST_ID = "REQUEST_ID";
    public static final String SDK_VERSION = "sdkVersion";
    public static final long TIMESTAMP = 100_000;

    private RequestContext requestContext;
    private MeIdStorage meIdStorage;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();
    private UUIDProvider uuidProvider;
    private TimestampProvider timestampProvider;

    @Before
    public void setup() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences");

        meIdStorage = mock(MeIdStorage.class);
        uuidProvider = mock(UUIDProvider.class);
        when(uuidProvider.provideId()).thenReturn(REQUEST_ID);

        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP);
        requestContext = new RequestContext(
                APPLICATION_CODE,
                APPLICATION_PASSWORD,
                1,
                new DeviceInfo(InstrumentationRegistry.getTargetContext(),
                        mock(HardwareIdProvider.class),
                        SDK_VERSION,
                        mock(LanguageProvider.class)),
                mock(AppLoginStorage.class),
                meIdStorage,
                mock(MeIdSignatureStorage.class),
                timestampProvider,
                uuidProvider);

        requestContext.setAppLoginParameters(new AppLoginParameters(3, "test@test.com"));
    }

    @After
    public void tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsCustomEvent_V3_mustNotBeNull() {
        RequestModelUtils.isCustomEvent_V3(null);
    }

    @Test
    public void testIsCustomEvent_V3_returnsTrue_ifIndeedV3Event() {
        RequestModel requestModel = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(VALID_CUSTOM_EVENT_V3)
                .build();

        assertTrue(RequestModelUtils.isCustomEvent_V3(requestModel));
    }

    @Test
    public void testIsCustomEvent_V3_returnsFalse_ifThereIsNoMatch() {
        RequestModel requestModel = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://www.google.com")
                .build();

        assertFalse(RequestModelUtils.isCustomEvent_V3(requestModel));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAppLogin_V2_requestContext_mustNotBeNull() {
        RequestModelUtils.createAppLogin_V2(null, "pushtoken");
    }

    @Test
    public void testCreateAppLogin_V2() {
        RequestModel expected = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://push.eservice.emarsys.net/api/mobileengage/v2/users/login")
                .payload(RequestPayloadUtils.createAppLoginPayload(requestContext, null))
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext))
                .build();

        RequestModel result = RequestModelUtils.createAppLogin_V2(requestContext, null);

        RequestModelTestUtils.assertEqualsRequestModels(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateLastMobileActivity_requestContext_mustNotBeNull() {
        RequestModelUtils.createLastMobileActivity(null);
    }

    @Test
    public void testCreateLastMobileActivity_V3() {
        when(meIdStorage.get()).thenReturn("meId");

        RequestModel expected = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(RequestUrlUtils.createEventUrl_V3(requestContext.getMeIdStorage().get()))
                .payload(RequestPayloadUtils.createBasePayload(requestContext))
                .headers(RequestHeaderUtils.createBaseHeaders_V3(requestContext))
                .build();

        RequestModel requestModel = RequestModelUtils.createLastMobileActivity(requestContext);

        assertEquals(expected.getUrl(), requestModel.getUrl());
        assertEquals("last_mobile_activity", (((List<Map<String, Object>>) requestModel.getPayload().get("events")).get(0).get("name")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInternalCustomEvent_eventNameShouldNotBeNull() {
        RequestModelUtils.createInternalCustomEvent(
                null,
                new HashMap<String, String>(),
                requestContext
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInternalCustomEvent_requestContextShouldNotBeNull() {
        RequestModelUtils.createInternalCustomEvent(
                "eventname",
                new HashMap<String, String>(),
                null
        );
    }

    @Test
    public void testCreateInternalCustomEvent_withoutAttributes() {
        long timestamp = 90_000;
        when(requestContext.getTimestampProvider().provideTimestamp()).thenReturn(timestamp);
        String eventName = "name";
        String meId = "12345";
        String meIdSignature = "12345";
        when(requestContext.getMeIdStorage().get()).thenReturn(meId);
        when(requestContext.getMeIdSignatureStorage().get()).thenReturn(meIdSignature);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "internal");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestamp));

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));

        RequestModel actual = RequestModelUtils.createInternalCustomEvent(
                eventName,
                null,
                requestContext);

        RequestModel expected = new RequestModel(
                RequestUrlUtils.createEventUrl_V3(meId),
                RequestMethod.POST,
                payload,
                RequestHeaderUtils.createBaseHeaders_V3(requestContext),
                timestamp,
                Long.MAX_VALUE,
                actual.getId());

        assertEquals(expected, actual);
    }

    @Test
    public void testCreateInternalCustomEvent_withAttributes() {
        long timestamp = 90_000;
        when(requestContext.getTimestampProvider().provideTimestamp()).thenReturn(timestamp);
        String eventName = "name";
        String meId = "12345";
        String meIdSignature = "12345";
        when(requestContext.getMeIdStorage().get()).thenReturn(meId);
        when(requestContext.getMeIdSignatureStorage().get()).thenReturn(meIdSignature);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        attributes.put("key3", "value3");

        Map<String, Object> event = new HashMap<>();
        event.put("type", "internal");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestamp));
        event.put("attributes", attributes);

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));

        RequestModel actual = RequestModelUtils.createInternalCustomEvent(
                eventName,
                attributes,
                requestContext);

        RequestModel expected = new RequestModel(
                RequestUrlUtils.createEventUrl_V3(meId),
                RequestMethod.POST,
                payload,
                RequestHeaderUtils.createBaseHeaders_V3(requestContext),
                timestamp,
                Long.MAX_VALUE,
                actual.getId());

        assertEquals(expected, actual);
    }
}