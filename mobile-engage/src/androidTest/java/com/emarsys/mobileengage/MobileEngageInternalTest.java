package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestIdProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.ExperimentalTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.mobileengage.util.RequestModelUtils;
import com.emarsys.mobileengage.util.RequestUrlUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.MobileEngageInternal.MOBILEENGAGE_SDK_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MobileEngageInternalTest {

    private static final long TIMESTAMP = 123;
    private static final String INTERNAL = "internal";
    private static final String CUSTOM = "custom";
    public static final String REQUEST_ID = "REQUEST_ID";

    private static String APPLICATION_ID = "user";
    private static String APPLICATION_SECRET = "pass";
    private static String ENDPOINT_BASE_V2 = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_BASE_V3 = "https://mobile-events.eservice.emarsys.net/v3/devices/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE_V2 + "users/login";
    private static String ENDPOINT_LOGOUT = ENDPOINT_BASE_V2 + "users/logout";
    private static String ENDPOINT_LAST_MOBILE_ACTIVITY = ENDPOINT_BASE_V2 + "events/ems_lastMobileActivity";
    private static String ME_ID = "ASD123";
    private static String ME_ID_SIGNATURE = "sig";

    private MobileEngageStatusListener statusListener;
    private MobileEngageCoreCompletionHandler coreCompletionHandler;
    private Map<String, String> defaultHeaders;
    private MobileEngageConfig baseConfig;
    private RequestManager manager;
    private Application application;
    private DeviceInfo deviceInfo;
    private AppLoginStorage appLoginStorage;
    private MobileEngageInternal mobileEngage;
    private MeIdStorage meIdStorage;
    private MeIdSignatureStorage meIdSignatureStorage;
    private AppLoginParameters appLoginParameters;
    private AppLoginParameters otherAppLoginParameters;
    private RequestContext requestContext;
    private TimestampProvider timestampProvider;
    private RequestIdProvider requestIdProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        MobileEngageExperimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);

        manager = mock(RequestManager.class);
        coreCompletionHandler = mock(MobileEngageCoreCompletionHandler.class);
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        deviceInfo = new DeviceInfo(application);
        appLoginStorage = new AppLoginStorage(application);
        appLoginStorage.remove();

        statusListener = mock(MobileEngageStatusListener.class);
        baseConfig = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(APPLICATION_ID, APPLICATION_SECRET)
                .statusListener(statusListener)
                .disableDefaultChannel()
                .build();

        defaultHeaders = RequestHeaderUtils.createDefaultHeaders(baseConfig);

        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP);
        requestIdProvider = mock(RequestIdProvider.class);
        when(requestIdProvider.provideId()).thenReturn(REQUEST_ID);

        meIdStorage = new MeIdStorage(application);
        meIdSignatureStorage = new MeIdSignatureStorage(application);
        requestContext = new RequestContext(
                baseConfig,
                deviceInfo,
                appLoginStorage,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider,
                requestIdProvider);

        mobileEngage = new MobileEngageInternal(
                baseConfig,
                manager,
                mock(Handler.class),
                coreCompletionHandler,
                requestContext);

        meIdStorage.set(ME_ID);
        meIdSignatureStorage.set(ME_ID_SIGNATURE);

        appLoginParameters = new AppLoginParameters(3, "test@test.com");
        otherAppLoginParameters = new AppLoginParameters(3, "test2@test.com");
    }

    @After
    public void tearDown() throws Exception {
        meIdStorage.remove();
        meIdSignatureStorage.remove();
        ExperimentalTestUtils.resetExperimentalFeatures();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_configShouldNotBeNull() {
        new MobileEngageInternal(
                null,
                manager,
                mock(Handler.class),
                coreCompletionHandler,
                mock(RequestContext.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestManagerShouldNotBeNull() {
        new MobileEngageInternal(
                baseConfig,
                null,
                mock(Handler.class),
                coreCompletionHandler,
                mock(RequestContext.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestContextShouldNotBeNull() {
        new MobileEngageInternal(
                baseConfig,
                manager,
                mock(Handler.class),
                coreCompletionHandler,
                null);
    }

    @Test
    public void testAppLogin_anonymous_requestManagerCalledWithCorrectRequestModel() {
        meIdStorage.remove();
        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        RequestModel expected = new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogin();

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogin_anonymous_returnsRequestModelId() {
        meIdStorage.remove();
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.appLogin();

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogin_withApploginParameters_requestManagerCalledWithCorrectRequestModel() {
        meIdStorage.remove();
        int contactFieldId = 3;
        String contactFieldValue = "value";
        RequestModel expected = createLoginRequestModel(new AppLoginParameters(contactFieldId, contactFieldValue));

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogin(contactFieldId, contactFieldValue);

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogin_returnsRequestModelId() {
        meIdStorage.remove();
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.appLogin(5, "value");

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogin_shouldSetAppLoginParametersOnRequestContext() {
        final RequestContext requestContext = mock(RequestContext.class, RETURNS_DEEP_STUBS);
        when(requestContext.getConfig()).thenReturn(baseConfig);
        when(requestContext.getRequestIdProvider()).thenReturn(requestIdProvider);

        MobileEngageInternal internal = new MobileEngageInternal(
                baseConfig,
                manager,
                mock(Handler.class),
                coreCompletionHandler,
                requestContext);

        final int contactFieldId = 3;
        final String contactFieldValue = "email@address.com";
        internal.appLogin(contactFieldId, contactFieldValue);

        ArgumentCaptor<AppLoginParameters> captor = ArgumentCaptor.forClass(AppLoginParameters.class);
        verify(requestContext).setAppLoginParameters(captor.capture());

        final AppLoginParameters appLoginParameters = captor.getValue();
        assertEquals(contactFieldId, appLoginParameters.getContactFieldId());
        assertEquals(contactFieldValue, appLoginParameters.getContactFieldValue());
    }

    @Test
    public void testAppLogin_shouldNotResult_inMultipleAppLoginRequests_evenIfPayloadIsTheSame() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        testSequentialApplogins(
                appLoginParameters,
                createLoginRequestModel(appLoginParameters),
                appLoginParameters,
                createLastMobileActivityRequestModel(appLoginParameters)
        );
    }

    @Test
    public void testAppLogin_shouldResult_inMultipleAppLoginRequests_ifPayloadIsDifferent() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        testSequentialApplogins(
                appLoginParameters,
                createLoginRequestModel(appLoginParameters),
                otherAppLoginParameters,
                createLoginRequestModel(otherAppLoginParameters)
        );
    }

    @Test
    public void testAppLogin_shouldResult_inMultipleAppLoginRequests_when_thereWasALogout() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogin(appLoginParameters.getContactFieldId(), appLoginParameters.getContactFieldValue());

        verify(manager).submit(captor.capture());
        RequestModel requestModel = captor.getValue();
        assertRequestModels(createLoginRequestModel(appLoginParameters), requestModel);

        clearInvocations(manager);

        mobileEngage.appLogout();

        clearInvocations(manager);

        captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogin(appLoginParameters.getContactFieldId(), appLoginParameters.getContactFieldValue());

        verify(manager).submit(captor.capture());
        requestModel = captor.getValue();
        assertRequestModels(createLoginRequestModel(appLoginParameters), requestModel);
    }

    @Test
    public void testAppLogin_shouldNotResult_inMultipleAppLoginRequests_ifPayloadIsTheSame_evenIfMobileEngageIsReInitialized() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        testSequentialApplogins_withReinstantiationOfMobileEngage(
                appLoginParameters,
                createLoginRequestModel(appLoginParameters),
                appLoginParameters,
                createLastMobileActivityRequestModel(appLoginParameters)
        );
    }

    @Test
    public void testAppLogin_shouldResult_inMultipleAppLoginRequests_ifPayloadIsDifferent_evenIfMobileEngageIsReInitialized() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        testSequentialApplogins_withReinstantiationOfMobileEngage(
                appLoginParameters,
                createLoginRequestModel(appLoginParameters),
                otherAppLoginParameters,
                createLoginRequestModel(otherAppLoginParameters)
        );
    }

    @Test
    public void testAppLogin_V3_shouldResult_inMultipleAppLoginRequests_ifPayloadIsTheSame_ifMeIdIsMissing() {

        testSequentialAppLogins_withMeIds(
                appLoginParameters,
                null,
                createLoginRequestModel(appLoginParameters),
                appLoginParameters,
                null,
                createLoginRequestModel(appLoginParameters)
        );
    }

    @Test
    public void testAppLogin_V3_shouldResult_inMultipleAppLoginRequests_ifPayloadIsDifferent_ifMeIdIsMissing() {

        testSequentialAppLogins_withMeIds(
                appLoginParameters,
                null,
                createLoginRequestModel(appLoginParameters),
                otherAppLoginParameters,
                null,
                createLoginRequestModel(otherAppLoginParameters)
        );
    }

    @Test
    public void testAppLogin_V3_shouldResult_inMultipleAppLoginRequests_ifPayloadIsDifferent_ifMeIdIsPresent() {
        testSequentialAppLogins_withMeIds(
                appLoginParameters,
                ME_ID,
                createLoginRequestModel(appLoginParameters),
                otherAppLoginParameters,
                ME_ID,
                createLoginRequestModel(otherAppLoginParameters)
        );
    }

    @Test
    public void testAppLogin_V3_shouldNotResult_inMultipleAppLoginRequests_ifPayloadIsTheSame_ifMeIdIsPresent() {
        testSequentialAppLogins_withMeIds(
                appLoginParameters,
                ME_ID,
                createLoginRequestModel(appLoginParameters),
                appLoginParameters,
                ME_ID,
                createLastMobileActivityRequestModelV3()
        );
    }

    @Test
    public void testAppLogin_V3_shouldResult_inAppLoginRequests_ifPayloadIsTheSame_ifMeIdIsPresentTheSecondTime() {
        testSequentialAppLogins_withMeIds(
                appLoginParameters,
                null,
                createLoginRequestModel(appLoginParameters),
                appLoginParameters,
                ME_ID,
                createLastMobileActivityRequestModelV3()
        );
    }

    @Test
    public void testAppLogout_requestManagerCalledWithCorrectRequestModel() {
        Map<String, Object> payload = createBasePayload();
        RequestModel expected = new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_LOGOUT)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogout();

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogout_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.appLogout();

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogout_removesStoredMeId() {
        MeIdStorage storage = new MeIdStorage(application);
        storage.set("testMeID");

        mobileEngage.appLogout();
        assertEquals(storage.get(), null);
    }

    @Test
    public void testAppLogout_removesStoredApploginParameters() {
        AppLoginStorage storage = new AppLoginStorage(application);
        storage.set(42);

        mobileEngage.appLogout();
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

        RequestModel expected = new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_BASE_V3 + ME_ID + "/events")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackCustomEvent(eventName, eventAttributes);

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();

        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testTrackCustomEvent_V3_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackCustomEvent("event", new HashMap<String, String>());

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testTrackCustomEvent_V2_requestManagerCalledWithCorrectRequestModel() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        String eventName = "cartoon";
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("tom", "jerry");

        Map<String, Object> payload = createBasePayload();
        payload.put("attributes", eventAttributes);

        RequestModel expected = new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_BASE_V2 + "events/" + eventName)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackCustomEvent(eventName, eventAttributes);

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();

        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testCustomEvent_V2_containsCredentials_fromAppLoginParameters() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        int contactFieldId = 3;
        String contactFieldValue = "test@test.com";
        requestContext.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackCustomEvent("customEvent", null);
        verify(manager).submit(captor.capture());

        Map<String, Object> payload = captor.getValue().getPayload();
        assertEquals(payload.get("contact_field_id"), contactFieldId);
        assertEquals(payload.get("contact_field_value"), contactFieldValue);
    }

    @Test
    public void testTrackCustomEvent_V2_returnsRequestModelId() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackCustomEvent("event", new HashMap<String, String>());

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackInternalCustomEvent_eventName_mustNotBeNull() {
        mobileEngage.trackInternalCustomEvent(null, new HashMap<String, String>());
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

        RequestModel expected = new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_BASE_V3 + ME_ID + "/events")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackInternalCustomEvent(eventName, eventAttributes);

        verify(manager).submit(captor.capture());

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

        RequestModel expected = new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_BASE_V3 + ME_ID + "/events")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackInternalCustomEvent(eventName, null);

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();

        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testTrackInternalCustomEvent_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackInternalCustomEvent("event", new HashMap<String, String>());

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testTrackInternalCustomEvent_requestManagerNotCalled_whenMeIdIsUnavailable() {
        meIdStorage.remove();
        mobileEngage.trackInternalCustomEvent("event", new HashMap<String, String>());

        verify(manager, times(0)).submit(any(RequestModel.class));
    }

    @Test
    public void testTrackInternalCustomEvent_requestManagerNotCalled_whenMeIdSignatureIsUnavailable() {
        meIdSignatureStorage.remove();
        mobileEngage.trackInternalCustomEvent("event", new HashMap<String, String>());

        verify(manager, times(0)).submit(any(RequestModel.class));
    }

    @Test
    public void testTrackMessageOpen_requestManagerCalledWithCorrectRequestModelWhenUsingV2() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
        Intent intent = getTestIntent();

        Map<String, Object> payload = createBasePayload();
        payload.put("sid", "+43c_lODSmXqCvdOz");

        RequestModel expected = new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_BASE_V2 + "events/message_open")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackMessageOpen(intent);

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testTrackMessageOpen_requestManagerCalledWithCorrectRequestModelWhenUsingV3() throws Exception {
        MobileEngageExperimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);

        Intent intent = getTestIntent();

        when(requestContext.getTimestampProvider().provideTimestamp()).thenReturn(TIMESTAMP);

        Map<String, Object> payload = createTrackMessageOpenPayload_V3();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackMessageOpen(intent);

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        RequestModel expected = new RequestModel(
                RequestUrlUtils.createEventUrl_V3(ME_ID),
                RequestMethod.POST,
                payload,
                RequestHeaderUtils.createBaseHeaders_V3(requestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                result.getId());
        assertRequestModels(expected, result);
    }

    @Test
    public void testTrackMessageOpen_returnsRequestModelId() throws Exception {
        Intent intent = getTestIntent();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackMessageOpen(intent);

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testTrackMessageOpen_containsCredentials_fromApploginParameters_V2() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        Intent intent = getTestIntent();
        int contactFieldId = 3;
        String contactFieldValue = "test@test.com";
        requestContext.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackMessageOpen(intent);
        verify(manager).submit(captor.capture());

        Map<String, Object> payload = captor.getValue().getPayload();
        assertEquals(contactFieldId, payload.get("contact_field_id"));
        assertEquals(contactFieldValue, payload.get("contact_field_value"));
    }

    @Test
    public void testGetMessageId_shouldReturnNullWithEmptyIntent() {
        String result = mobileEngage.getMessageId(new Intent());
        assertNull(result);
    }

    @Test
    public void testGetMessageId_shoudReturnTheCorrectSIDValue() throws Exception {
        Intent intent = getTestIntent();
        String result = mobileEngage.getMessageId(intent);
        assertEquals("+43c_lODSmXqCvdOz", result);
    }

    @Test
    public void testSetPushToken_whenApploginParameters_isEmpty() {
        MobileEngageInternal spy = spy(mobileEngage);

        requestContext.setAppLoginParameters(new AppLoginParameters());
        spy.setPushToken("123456789");

        verify(spy, times(1)).sendAppLogin();
    }

    @Test
    public void testSetPushToken_whenApploginParameters_hasCredentials() {
        int contactFieldId = 12;
        String contactFieldValue = "asdf";
        MobileEngageInternal spy = spy(mobileEngage);

        requestContext.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        spy.setPushToken("123456789");

        verify(spy, times(1)).sendAppLogin();
    }

    @Test
    public void testSetPushToken_doesNotCallAppLogins_whenApploginParameters_isNull() {
        MobileEngageInternal spy = spy(mobileEngage);

        requestContext.setAppLoginParameters(null);
        spy.setPushToken("123456789");

        verify(spy, times(0)).sendAppLogin();
    }

    private void testSequentialApplogins(
            AppLoginParameters firstAppLoginParameter,
            RequestModel firstExpectedRequestModel,
            AppLoginParameters secondAppLoginParameter,
            RequestModel secondExpectedRequestModel) {
        testSequentialApplogins(
                firstAppLoginParameter,
                null,
                firstExpectedRequestModel,
                secondAppLoginParameter,
                null,
                secondExpectedRequestModel,
                false
        );
    }

    private void testSequentialApplogins_withReinstantiationOfMobileEngage(
            AppLoginParameters firstAppLoginParameter,
            RequestModel firstExpectedRequestModel,
            AppLoginParameters secondAppLoginParameter,
            RequestModel secondExpectedRequestModel) {
        testSequentialApplogins(
                firstAppLoginParameter,
                null,
                firstExpectedRequestModel,
                secondAppLoginParameter,
                null,
                secondExpectedRequestModel,
                true
        );
    }

    private void testSequentialAppLogins_withMeIds(
            AppLoginParameters firstAppLoginParameter,
            String firstMeId,
            RequestModel firstExpectedRequestModel,
            AppLoginParameters secondAppLoginParameter,
            String secondMeId,
            RequestModel secondExpectedRequestModel
    ) {
        testSequentialApplogins(
                firstAppLoginParameter,
                firstMeId,
                firstExpectedRequestModel,
                secondAppLoginParameter,
                secondMeId,
                secondExpectedRequestModel,
                false
        );
    }

    private void testSequentialApplogins(
            AppLoginParameters firstAppLoginParameter,
            String firstMeId,
            RequestModel firstExpectedRequestModel,
            AppLoginParameters secondAppLoginParameter,
            String secondMeId,
            RequestModel secondExpectedRequestModel,
            boolean shouldReinstantiateMobileEngage) {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        meIdStorage.set(firstMeId);
        mobileEngage.appLogin(firstAppLoginParameter.getContactFieldId(), firstAppLoginParameter.getContactFieldValue());

        verify(manager).submit(captor.capture());
        RequestModel requestModel = captor.getValue();
        assertRequestModels(firstExpectedRequestModel, requestModel);

        clearInvocations(manager);

        if (shouldReinstantiateMobileEngage) {
            mobileEngage = new MobileEngageInternal(
                    baseConfig,
                    manager,
                    mock(Handler.class),
                    coreCompletionHandler,
                    requestContext);
        }

        captor = ArgumentCaptor.forClass(RequestModel.class);

        requestContext.setAppLoginParameters(secondAppLoginParameter);
        meIdStorage.set(secondMeId);
        mobileEngage.appLogin(secondAppLoginParameter.getContactFieldId(), secondAppLoginParameter.getContactFieldValue());

        verify(manager).submit(captor.capture());
        requestModel = captor.getValue();
        assertRequestModels(secondExpectedRequestModel, requestModel);
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

        String pushToken = mobileEngage.getPushToken();
        if (pushToken == null) {
            payload.put("push_token", false);
        } else {
            payload.put("push_token", pushToken);
        }

        return payload;
    }

    private RequestModel createLoginRequestModel(AppLoginParameters appLoginParameters) {
        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        payload.put("contact_field_id", appLoginParameters.getContactFieldId());
        payload.put("contact_field_value", appLoginParameters.getContactFieldValue());
        return new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .headers(defaultHeaders)
                .build();
    }

    private RequestModel createLastMobileActivityRequestModel(AppLoginParameters appLoginParameters) {
        Map<String, Object> payload = createBasePayload();
        payload.put("contact_field_id", appLoginParameters.getContactFieldId());
        payload.put("contact_field_value", appLoginParameters.getContactFieldValue());
        return new RequestModel.Builder(timestampProvider, requestIdProvider)
                .url(ENDPOINT_LAST_MOBILE_ACTIVITY)
                .payload(payload)
                .headers(defaultHeaders)
                .build();
    }

    private RequestModel createLastMobileActivityRequestModelV3() {
        return RequestModelUtils.createInternalCustomEvent("last_mobile_activity", null, requestContext);
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
        event.put("name", "message_open");

        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("sid", "+43c_lODSmXqCvdOz");

        event.put("attributes", attributes);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(TIMESTAMP));

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));
        return payload;
    }
}