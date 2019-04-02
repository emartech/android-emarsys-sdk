package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.DefaultCoreCompletionHandler;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.device.LanguageProvider;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.provider.version.VersionProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestHeaderUtils_Old;
import com.emarsys.mobileengage.util.RequestUrlUtils_Old;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.SharedPrefsUtils;
import com.emarsys.testUtil.TimeoutUtils;
import com.emarsys.testUtil.mockito.ThreadSpy;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.emarsys.mobileengage.MobileEngageInternal_V3_Old.MOBILEENGAGE_SDK_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MobileEngageInternal_V3_OldTest {

    private static final long TIMESTAMP = 123;
    private static final String INTERNAL = "internal";
    private static final String CUSTOM = "custom";
    public static final String REQUEST_ID = "REQUEST_ID";
    public static final int CONTACT_FIELD_ID = Integer.MAX_VALUE;

    private static String APPLICATION_ID = "user";
    private static String APPLICATION_PASSWORD = "pass";
    private static String ENDPOINT_BASE_V2 = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_BASE_V3 = "https://mobile-events.eservice.emarsys.net/v3/devices/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE_V2 + "users/login";
    private static String ENDPOINT_LOGOUT = ENDPOINT_BASE_V2 + "users/logout";
    private static String ENDPOINT_LAST_MOBILE_ACTIVITY = ENDPOINT_BASE_V2 + "events/ems_lastMobileActivity";
    private static String ME_ID = "ASD123";
    private static String ME_ID_SIGNATURE = "sig";

    private DefaultCoreCompletionHandler coreCompletionHandler;
    private Map<String, String> defaultHeaders;
    private RequestManager manager;
    private Application application;
    private DeviceInfo deviceInfo;
    private AppLoginStorage appLoginStorage;
    private MobileEngageInternal_V3_Old mobileEngageInternal;
    private MeIdStorage meIdStorage;
    private MeIdSignatureStorage meIdSignatureStorage;

    private RequestContext requestContext;
    private TimestampProvider timestampProvider;
    private UUIDProvider uuidProvider;
    private CompletionListener mockCompletionListener;
    private SharedPreferences sharedPreferences;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences");

        mockCompletionListener = mock(CompletionListener.class);

        manager = mock(RequestManager.class);
        coreCompletionHandler = mock(DefaultCoreCompletionHandler.class);
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        sharedPreferences = application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE);

        deviceInfo = new DeviceInfo(application,
                mock(HardwareIdProvider.class),
                mock(VersionProvider.class),
                mock(LanguageProvider.class));
        appLoginStorage = new AppLoginStorage(sharedPreferences);
        appLoginStorage.remove();

        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP);
        uuidProvider = mock(UUIDProvider.class);
        when(uuidProvider.provideId()).thenReturn(REQUEST_ID);

        meIdStorage = new MeIdStorage(sharedPreferences);
        meIdSignatureStorage = new MeIdSignatureStorage(sharedPreferences);
        requestContext = new RequestContext(
                APPLICATION_ID,
                APPLICATION_PASSWORD,
                CONTACT_FIELD_ID,
                deviceInfo,
                appLoginStorage,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider,
                uuidProvider,
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class)
        );

        defaultHeaders = RequestHeaderUtils_Old.createDefaultHeaders(requestContext);

        mobileEngageInternal = new MobileEngageInternal_V3_Old(
                manager,
                new Handler(Looper.getMainLooper()),
                coreCompletionHandler,
                requestContext);

        meIdStorage.set(ME_ID);
        meIdSignatureStorage.set(ME_ID_SIGNATURE);
    }

    @After
    public void tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestManagerShouldNotBeNull() {
        new MobileEngageInternal_V3_Old(
                null,
                mock(Handler.class),
                coreCompletionHandler,
                mock(RequestContext.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestContextShouldNotBeNull() {
        new MobileEngageInternal_V3_Old(
                manager,
                mock(Handler.class),
                coreCompletionHandler,
                null);
    }

    @Test
    public void testAppLogin_anonymous_requestManagerCalledWithCorrectRequestModel() {
        meIdStorage.remove();
        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        RequestModel expected = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngageInternal.setAnonymousContact(null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogin_anonymous_requestManagerCalledWithCorrectCompletionHandler() {
        meIdStorage.remove();

        CompletionListener completionListener = mock(CompletionListener.class);

        mobileEngageInternal.setAnonymousContact(completionListener);

        verify(manager).submit(any(RequestModel.class), eq(completionListener));
    }

    @Test
    public void testAppLogin_anonymous_returnsRequestModelId() {
        meIdStorage.remove();
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngageInternal.setAnonymousContact(null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogin_withContactFieldValue_requestManagerCalledWithCorrectCompletionHandler() {
        meIdStorage.remove();
        String contactFieldValue = "value";

        CompletionListener completionListener = mock(CompletionListener.class);

        mobileEngageInternal.setContact(contactFieldValue, completionListener);

        verify(manager).submit(any(RequestModel.class), eq(completionListener));
    }

    @Test
    public void testAppLogout_requestManagerCalledWithCorrectRequestModel() {
        Map<String, Object> payload = createBasePayload();
        RequestModel expected = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(ENDPOINT_LOGOUT)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngageInternal.clearContact(null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogout_requestManagerCalledWithCompletionListener() {
        CompletionListener completionListener = mock(CompletionListener.class);

        mobileEngageInternal.clearContact(completionListener);

        verify(manager).submit(any(RequestModel.class), eq(completionListener));
    }

    @Test
    public void testAppLogout_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngageInternal.clearContact(null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogout_removesStoredAppLoginParameters() {
        AppLoginStorage storage = new AppLoginStorage(sharedPreferences);
        storage.set(42);

        mobileEngageInternal.clearContact(null);
        assertEquals(storage.get(), null);
    }

    @Test
    public void testAppLogout_removesStoredMeId() {
        MeIdStorage storage = new MeIdStorage(sharedPreferences);
        storage.set("testMeID");

        mobileEngageInternal.clearContact(null);
        assertEquals(storage.get(), null);
    }

    @Test
    public void testAppLogout_removesStoredMeIdSignature() {
        MeIdSignatureStorage storage = new MeIdSignatureStorage(sharedPreferences);
        storage.set("testMeID");

        mobileEngageInternal.clearContact(null);
        assertEquals(storage.get(), null);
    }

    @Test
    public void testTrackCustomEvent_V3_requestManagerCalledWithCorrectRequestModel() {
        String eventName = "cartoon";
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("tom", "jerry");

        Map<String, Object> payload = createEventPayload(
                eventName,
                eventAttributes,
                CUSTOM);

        RequestModel expected = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(ENDPOINT_BASE_V3 + ME_ID + "/events")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngageInternal.trackCustomEvent(eventName, eventAttributes, null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        RequestModel result = captor.getValue();

        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testTrackCustomEvent_V3_requestManagerCalledWithCorrectCompletionHandler() {
        String eventName = "cartoon";

        CompletionListener completionListener = mock(CompletionListener.class);

        mobileEngageInternal.trackCustomEvent(eventName, null, completionListener);

        verify(manager).submit(any(RequestModel.class), eq(completionListener));
    }

    @Test
    public void testTrackCustomEvent_V3_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngageInternal.trackCustomEvent("event", new HashMap<String, String>(), null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackInternalCustomEvent_eventName_mustNotBeNull() {
        mobileEngageInternal.trackInternalCustomEvent(null, new HashMap<String, String>(), null);
    }

    @Test
    public void testTrackInternalCustomEvent_requestManagerCalledWithCorrectRequestModel() {
        String eventName = "cartoon";
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("tom", "jerry");

        Map<String, Object> payload = createEventPayload(
                eventName,
                eventAttributes,
                INTERNAL);

        RequestModel expected = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(ENDPOINT_BASE_V3 + ME_ID + "/events")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngageInternal.trackInternalCustomEvent(eventName, eventAttributes, null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        RequestModel result = captor.getValue();

        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testTrackInternalCustomEvent_requestManagerCalledWithCorrectRequestModel_withoutAttributes() {
        String eventName = "cartoon";

        Map<String, Object> payload = createEventPayload(
                eventName,
                null,
                INTERNAL);

        RequestModel expected = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(ENDPOINT_BASE_V3 + ME_ID + "/events")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngageInternal.trackInternalCustomEvent(eventName, null, null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        RequestModel result = captor.getValue();

        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testTrackInternalCustomEvent_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngageInternal.trackInternalCustomEvent("event", new HashMap<String, String>(), null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testTrackInternalCustomEvent_requestManagerNotCalled_whenMeIdIsUnavailable() {
        meIdStorage.remove();
        mobileEngageInternal.trackInternalCustomEvent("event", new HashMap<String, String>(), null);

        verify(manager, times(0)).submit(any(RequestModel.class), (CompletionListener) isNull());
    }

    @Test
    public void testTrackInternalCustomEvent_requestManagerNotCalled_whenMeIdSignatureIsUnavailable() {
        meIdSignatureStorage.remove();
        mobileEngageInternal.trackInternalCustomEvent("event", new HashMap<String, String>(), null);

        verify(manager, times(0)).submit(any(RequestModel.class), (CompletionListener) isNull());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackMessageOpen_intent_mustNotBeNull() {
        mobileEngageInternal.trackMessageOpen(null, null);
    }

    @Test
    public void testTrackMessageOpen_requestManagerCalledWithCorrectRequestModelWhenUsingV3() {
        Intent intent = getTestIntent();

        when(requestContext.getTimestampProvider().provideTimestamp()).thenReturn(TIMESTAMP);

        Map<String, Object> payload = createTrackMessageOpenPayload_V3();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngageInternal.trackMessageOpen(intent, null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        RequestModel result = captor.getValue();
        RequestModel expected = new RequestModel(
                RequestUrlUtils_Old.createEventUrl_V3(ME_ID),
                RequestMethod.POST,
                payload,
                RequestHeaderUtils_Old.createBaseHeaders_V3(requestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                result.getId());
        assertRequestModels(expected, result);
    }

    @Test
    public void testTrackMessageOpen_requestManagerCalledWithCorrectCompletionHandler_whenUsingV3() {
        Intent intent = getTestIntent();

        when(requestContext.getTimestampProvider().provideTimestamp()).thenReturn(TIMESTAMP);

        CompletionListener completionListener = mock(CompletionListener.class);

        mobileEngageInternal.trackMessageOpen(intent, completionListener);

        verify(manager).submit(any(RequestModel.class), eq(completionListener));
    }

    @Test
    public void testTrackMessageOpen_returnsRequestModelId() {
        Intent intent = getTestIntent();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngageInternal.trackMessageOpen(intent, null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());
    }

    @Test
    public void testTrackMessageOpen_withEmptyIntent_shouldCallCompletionListener_withIllegalArgumentException() {
        CompletionListener completionListener = mock(CompletionListener.class);

        mobileEngageInternal.trackMessageOpen(new Intent(), completionListener);

        verify(completionListener, Mockito.timeout(100)).onCompleted(any(IllegalArgumentException.class));
    }

    @Test
    public void testTrackMessageOpen_withEmptyIntent_shouldCallCompletionListener_onMainThread() {
        CompletionListener completionListener = mock(CompletionListener.class);
        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(completionListener).onCompleted(any(Throwable.class));

        mobileEngageInternal.trackMessageOpen(new Intent(), completionListener);

        threadSpy.verifyCalledOnMainThread();
    }

    @Test
    public void testGetMessageId_shouldReturnNull_withEmptyIntent() {
        String result = mobileEngageInternal.getMessageId(new Intent());
        assertNull(result);
    }

    @Test
    public void testGetMessageId_shouldReturnNull_withMissingUParam() {
        Intent intent = new Intent();
        Bundle bundlePayload = new Bundle();
        bundlePayload.putString("key1", "value1");
        intent.putExtra("payload", bundlePayload);
        String result = mobileEngageInternal.getMessageId(intent);
        assertNull(result);
    }

    @Test
    public void testGetMessageId_shouldReturnNull_withMissingSIDParam() {
        Intent intent = new Intent();
        Bundle bundlePayload = new Bundle();
        bundlePayload.putString("key1", "value1");
        bundlePayload.putString("u", "{}");
        intent.putExtra("payload", bundlePayload);
        String result = mobileEngageInternal.getMessageId(intent);
        assertNull(result);
    }


    @Test
    public void testGetMessageId_shouldReturnNull_withInvalidJson() {
        Intent intent = new Intent();
        Bundle bundlePayload = new Bundle();
        bundlePayload.putString("key1", "value1");
        bundlePayload.putString("u", "{invalidJson}");
        intent.putExtra("payload", bundlePayload);
        String result = mobileEngageInternal.getMessageId(intent);
        assertNull(result);
    }


    @Test
    public void testGetMessageId_shouldReturnTheCorrectSIDValue() {
        Intent intent = getTestIntent();
        String result = mobileEngageInternal.getMessageId(intent);
        assertEquals("+43c_lODSmXqCvdOz", result);
    }

    @Test
    public void testSetPushToken_whenAppLoginParameters_isEmpty() {
        MobileEngageInternal_V3_Old spy = spy(mobileEngageInternal);

        spy.setPushToken("123456789", null);

        verify(spy).sendAppLogin((CompletionListener) isNull());
    }

    @Test
    public void testSetPushToken_whenAppLoginParameters_hasCredentials() {
        int contactFieldId = 12;
        String contactFieldValue = "asdf";
        MobileEngageInternal_V3_Old spy = spy(mobileEngageInternal);

        spy.setPushToken("123456789", null);

        verify(spy).sendAppLogin((CompletionListener) isNull());
    }

    @Test
    public void testSetPushToken_doesNotCallAppLogins_whenApploginParameters_isNull() {
        MobileEngageInternal_V3_Old spy = spy(mobileEngageInternal);

        spy.setPushToken("123456789", null);

        verify(spy, times(0)).sendAppLogin(any(CompletionListener.class));
    }

    private Intent getTestIntent() {
        Intent intent = new Intent();
        Bundle bundlePayload = new Bundle();
        bundlePayload.putString("key1", "value1");
        bundlePayload.putString("u", "{\"sid\": \"+43c_lODSmXqCvdOz\"}");
        intent.putExtra("payload", bundlePayload);
        return intent;
    }

    private Map<String, Object> createBasePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", APPLICATION_ID);
        payload.put("hardware_id", deviceInfo.getHwid());

        return payload;
    }

    private Map<String, Object> injectLoginPayload(Map<String, Object> payload) {
        payload.put("platform", deviceInfo.getPlatform());
        payload.put("language", deviceInfo.getLanguage());
        payload.put("timezone", deviceInfo.getTimezone());
        payload.put("device_model", deviceInfo.getModel());
        payload.put("application_version", deviceInfo.getApplicationVersion());
        payload.put("os_version", deviceInfo.getOsVersion());
        payload.put("ems_sdk", MOBILEENGAGE_SDK_VERSION);

        String pushToken = mobileEngageInternal.getPushToken();
        if (pushToken == null) {
            payload.put("push_token", false);
        } else {
            payload.put("push_token", pushToken);
        }

        return payload;
    }

    private void assertRequestModels(RequestModel expected, RequestModel result) {
        assertEquals(expected.getUrl(), result.getUrl());
        assertEquals(expected.getMethod(), result.getMethod());
        assertEquals(expected.getPayload(), result.getPayload());
    }

    private void assertRequestModels_withPayloadAsString(RequestModel expected, RequestModel result) {
        assertEquals(expected.getUrl(), result.getUrl());
        assertEquals(expected.getMethod(), result.getMethod());
        assertEquals(expected.getPayload().toString(), result.getPayload().toString());
    }

    private Map<String, Object> createEventPayload(String eventName, Map<String, String> eventAttributes, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", eventType);
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(TIMESTAMP));
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            event.put("attributes", eventAttributes);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));
        return payload;
    }

    @NonNull
    private Map<String, Object> createTrackMessageOpenPayload_V3() {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "internal");
        event.put("name", "push:click");

        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("sid", "+43c_lODSmXqCvdOz");
        attributes.put("origin", "main");

        event.put("attributes", attributes);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(TIMESTAMP));

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));
        return payload;
    }
}