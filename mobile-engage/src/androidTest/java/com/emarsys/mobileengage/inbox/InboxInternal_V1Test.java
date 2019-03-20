package com.emarsys.mobileengage.inbox;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.Registry;
import com.emarsys.core.api.ResponseErrorException;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.device.DeviceInfo;
import com.emarsys.core.device.LanguageProvider;
import com.emarsys.core.provider.hardwareid.HardwareIdProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.provider.version.VersionProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.factory.CompletionProxyFactory;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.storage.Storage;
import com.emarsys.core.worker.Worker;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.fake.FakeInboxResultListener;
import com.emarsys.mobileengage.fake.FakeResetBadgeCountResultListener;
import com.emarsys.mobileengage.fake.FakeRestClient;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestHeaderUtils_Old;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.SharedPrefsUtils;
import com.emarsys.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.mobileengage.fake.FakeInboxResultListener.Mode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InboxInternal_V1Test {

    private static final String HARDWARE_ID = "hwid";
    private static final String APPLICATION_ID = "id";
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final long TIMESTAMP = 100_000;
    private static List<Notification> notificationList;

    private ResultListener<Try<NotificationInboxStatus>> resultListenerMock;
    private CompletionListener resetListenerMock;
    private Map<String, String> defaultHeaders;
    private RequestManager manager;
    private CountDownLatch latch;
    private InboxInternal_V1 inbox;

    private AppLoginParameters appLoginParameters_withCredentials;
    private AppLoginParameters appLoginParameters_noCredentials;
    private AppLoginParameters appLoginParameters_missing;

    private Application application;
    private NotificationCache cache;
    private DeviceInfo deviceInfo;
    private RequestContext requestContext;
    private UUIDProvider uuidProvider;
    private TimestampProvider timestampProvider;
    private LanguageProvider languageProvider;
    private HardwareIdProvider hardwareIdProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        latch = new CountDownLatch(1);

        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

        manager = mock(RequestManager.class);

        notificationList = createNotificationList();
        hardwareIdProvider = mock(HardwareIdProvider.class);
        languageProvider = mock(LanguageProvider.class);

        when(hardwareIdProvider.provideHardwareId()).thenReturn(HARDWARE_ID);
        deviceInfo = new DeviceInfo(application, hardwareIdProvider, mock(VersionProvider.class), languageProvider);

        uuidProvider = mock(UUIDProvider.class);
        when(uuidProvider.provideId()).thenReturn(REQUEST_ID);

        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP);

        requestContext = new RequestContext(
                APPLICATION_ID,
                "applicationPassword",
                1,
                deviceInfo,
                new AppLoginStorage(application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                timestampProvider,
                uuidProvider,
                mock(Storage.class),
                mock(Storage.class),
                mock(Storage.class));

        defaultHeaders = RequestHeaderUtils_Old.createDefaultHeaders(requestContext);

        inbox = new InboxInternal_V1(manager, requestContext);

        resultListenerMock = mock(ResultListener.class);
        resetListenerMock = mock(CompletionListener.class);
        appLoginParameters_withCredentials = new AppLoginParameters(30, "value");
        appLoginParameters_noCredentials = new AppLoginParameters();
        appLoginParameters_missing = null;

        Field cacheField = NotificationCache.class.getDeclaredField("internalCache");
        cacheField.setAccessible(true);
        ((List) cacheField.get(null)).clear();

        cache = new NotificationCache();
    }

    @After
    public void tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestManager_shouldNotBeNull() {
        new InboxInternal_V1(null, requestContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestContext_shouldNotBeNull() {
        new InboxInternal_V1(manager, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFetchNotifications_listenerShouldNotBeNull() {
        inbox.fetchNotifications(null);
    }

    @Test
    public void testFetchNotifications_shouldMakeRequest_viaRequestManager_submitNow() {
        RequestModel expected = createRequestModel("https://me-inbox.eservice.emarsys.net/api/notifications", RequestMethod.GET);

        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);
        inbox.fetchNotifications(resultListenerMock);

        ArgumentCaptor<RequestModel> requestCaptor = ArgumentCaptor.forClass(RequestModel.class);
        verify(manager).submitNow(requestCaptor.capture(), any(CoreCompletionHandler.class));

        RequestModel requestModel = requestCaptor.getValue();
        Assert.assertNotNull(requestModel.getId());
        Assert.assertNotNull(requestModel.getTimestamp());
        Assert.assertEquals(expected.getUrl(), requestModel.getUrl());
        Assert.assertEquals(expected.getHeaders(), requestModel.getHeaders());
        Assert.assertEquals(expected.getMethod(), requestModel.getMethod());
    }

    @Test
    public void testFetchNotifications_listener_success() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext
        );

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(new NotificationInboxStatus(notificationList, 300), listener.resultStatus);
        Assert.assertEquals(1, listener.successCount);
    }

    @Test
    public void testFetchNotifications_listener_success_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext
        );

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.successCount);
    }

    @Test
    public void testFetchNotifications_listener_success_withCachedNotifications() throws Exception {
        List<Notification> cachedNotifications = createCacheList();
        for (int i = cachedNotifications.size() - 1; i >= 0; --i) {
            cache.cache(cachedNotifications.get(i));
        }

        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext
        );

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        List<Notification> result = listener.resultStatus.getNotifications();

        List<Notification> expected = new ArrayList<>(cachedNotifications);
        expected.addAll(createNotificationList());

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testFetchNotifications_listener_failureWithException() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        Exception expectedException = new Exception("FakeRestClientException");
        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(expectedException)),
                requestContext
        );

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(expectedException, listener.errorCause);
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotifications_listener_failureWithException_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(new Exception())),
                requestContext
        );

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithResponseModel() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel.class))
                .build();

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext
        );

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        ResponseErrorException expectedException = new ResponseErrorException(
                responseModel.getStatusCode(),
                responseModel.getMessage(),
                responseModel.getBody());

        ResponseErrorException resultException = (ResponseErrorException) listener.errorCause;
        Assert.assertEquals(expectedException.getStatusCode(), resultException.getStatusCode());
        Assert.assertEquals(expectedException.getMessage(), resultException.getMessage());
        Assert.assertEquals(expectedException.getBody(), resultException.getBody());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithResponseModel_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel.class))
                .build();
        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext
        );

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithParametersNotSet() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_missing);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(NotificationInboxException.class, listener.errorCause.getClass());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithParametersNotSet_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_missing);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithParametersSet_butLacksCredentials() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_noCredentials);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(NotificationInboxException.class, listener.errorCause.getClass());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testFetchNotification_listener_failureWithParametersSet_butLacksCredentials_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_noCredentials);

        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_shouldMakeRequest_viaRequestManager_submitNow() {
        RequestModel expected = createRequestModel("https://me-inbox.eservice.emarsys.net/api/reset-badge-count", RequestMethod.POST);

        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);
        inbox.resetBadgeCount(resetListenerMock);

        ArgumentCaptor<RequestModel> requestCaptor = ArgumentCaptor.forClass(RequestModel.class);
        verify(manager).submitNow(requestCaptor.capture(), any(CoreCompletionHandler.class));

        RequestModel requestModel = requestCaptor.getValue();
        Assert.assertNotNull(requestModel.getId());
        Assert.assertNotNull(requestModel.getTimestamp());
        Assert.assertEquals(expected.getUrl(), requestModel.getUrl());
        Assert.assertEquals(expected.getHeaders(), requestModel.getHeaders());
        Assert.assertEquals(expected.getMethod(), requestModel.getMethod());
    }

    @Test
    public void testResetBadgeCount_listener_success() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext
        );

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(1, listener.successCount);
    }

    @Test
    public void testResetBadgeCount_listener_success_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext
        );

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(1, listener.successCount);
    }

    @Test
    public void testResetBadgeCount_listener_failureWithException() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        Exception expectedException = new Exception("FakeRestClientException");
        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(expectedException)),
                requestContext
        );

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(expectedException, listener.errorCause);
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_listener_failureWithException_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(new Exception())),
                requestContext
        );

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_listener_failureWithResponseModel() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel.class))
                .build();

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext
        );

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch);
        inbox.resetBadgeCount(listener);

        latch.await();

        ResponseErrorException expectedException = new ResponseErrorException(
                responseModel.getStatusCode(),
                responseModel.getMessage(),
                responseModel.getBody());

        ResponseErrorException resultException = (ResponseErrorException) listener.errorCause;
        Assert.assertEquals(expectedException.getStatusCode(), resultException.getStatusCode());
        Assert.assertEquals(expectedException.getMessage(), resultException.getMessage());
        Assert.assertEquals(expectedException.getBody(), resultException.getBody());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_listener_failureWithResponseModel_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel.class))
                .build();
        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext
        );

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_listener_failureWithParametersNotSet() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_missing);

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(NotificationInboxException.class, listener.errorCause.getClass());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_listener_failureWithParametersNotSet_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_missing);

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_listener_failureWithParametersSet_butLacksCredentials() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_noCredentials);

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(NotificationInboxException.class, listener.errorCause.getClass());
        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_listener_failureWithParametersSet_butLacksCredentials_shouldBeCalledOnMainThread() throws InterruptedException {
        requestContext.setAppLoginParameters(appLoginParameters_noCredentials);

        FakeResetBadgeCountResultListener listener = new FakeResetBadgeCountResultListener(latch, FakeResetBadgeCountResultListener.Mode.MAIN_THREAD);
        inbox.resetBadgeCount(listener);

        latch.await();

        Assert.assertEquals(1, listener.errorCount);
    }

    @Test
    public void testResetBadgeCount_shouldNotFail_withNullListener_success() {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(createSuccessResponse(), FakeRestClient.Mode.SUCCESS)),
                requestContext
        );

        try {
            inbox.resetBadgeCount(null);
            Thread.sleep(150);
        } catch (Exception e) {
            Assert.fail("Should not throw exception!");
        }
    }

    @Test
    public void testResetBadgeCount_shouldNotFail_withNullListener_failureWithException() {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        Exception expectedException = new Exception("FakeRestClientException");
        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(expectedException)),
                requestContext
        );

        try {
            inbox.resetBadgeCount(null);
            Thread.sleep(150);
        } catch (Exception e) {
            Assert.fail("Should not throw exception!");
        }
    }

    @Test
    public void testResetBadgeCount_shouldNotFail_withNullListener_failureWithResponseModel() {
        requestContext.setAppLoginParameters(appLoginParameters_withCredentials);

        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(400)
                .message("Bad request")
                .requestModel(mock(RequestModel.class))
                .build();
        inbox = new InboxInternal_V1(
                requestManagerWithRestClient(new FakeRestClient(responseModel, FakeRestClient.Mode.ERROR_RESPONSE_MODEL)),
                requestContext
        );

        try {
            inbox.resetBadgeCount(null);
            Thread.sleep(150);
        } catch (Exception e) {
            Assert.fail("Should not throw exception!");
        }
    }

    @Test
    public void testResetBadgeCount_shouldNotFail_withNullListener_failureWithParametersNotSet() {
        requestContext.setAppLoginParameters(appLoginParameters_missing);

        try {
            inbox.resetBadgeCount(null);
            Thread.sleep(150);
        } catch (Exception e) {
            Assert.fail("Should not throw exception!");
        }
    }

    @Test
    public void testResetBadgeCount_shouldNotFail_withNullListener_failureWithParametersSet_butLacksCredentials() {
        requestContext.setAppLoginParameters(appLoginParameters_noCredentials);

        try {
            inbox.resetBadgeCount(null);
            Thread.sleep(150);
        } catch (Exception e) {
            Assert.fail("Should not throw exception!");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackNotificationOpen_notification_mustNotBeNull() {
        inbox.trackNotificationOpen(null, mock(CompletionListener.class));
    }

    @Test
    public void testTrackNotificationOpen_requestManagerCalledWithCorrectRequestModel() {
        Notification message = new Notification("id1", "sid1", "title", null, new HashMap<String, String>(), new JSONObject(), 7200, new Date().getTime());

        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", APPLICATION_ID);
        payload.put("hardware_id", deviceInfo.getHwid());
        payload.put("sid", "sid1");
        payload.put("source", "inbox");

        RequestModel expected = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://push.eservice.emarsys.net/api/mobileengage/v2/events/message_open")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        inbox.trackNotificationOpen(message, null);

        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        RequestModel result = captor.getValue();
        Assert.assertEquals(expected.getUrl(), result.getUrl());
        Assert.assertEquals(expected.getMethod(), result.getMethod());
        Assert.assertEquals(expected.getPayload(), result.getPayload());
    }

    @Test
    public void testTrackNotificationOpen_requestManagerCalledWithCorrectCompletionListener() {
        Notification message = new Notification(
                "id1",
                "sid1",
                "title",
                null,
                new HashMap<String, String>(),
                new JSONObject(),
                7200,
                new Date().getTime());

        CompletionListener completionListener = mock(CompletionListener.class);

        inbox.trackNotificationOpen(message, completionListener);

        verify(manager).submit(any(RequestModel.class), eq(completionListener));
    }

    @Test
    public void testTrackNotificationOpen_containsCredentials_fromApploginParameters() {
        int contactFieldId = 3;
        String contactFieldValue = "test@test.com";
        requestContext.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        inbox.trackNotificationOpen(mock(Notification.class), null);
        verify(manager).submit(captor.capture(), (CompletionListener) isNull());

        Map<String, Object> payload = captor.getValue().getPayload();
        Assert.assertEquals(payload.get("contact_field_id"), contactFieldId);
        Assert.assertEquals(payload.get("contact_field_value"), contactFieldValue);
    }

    private RequestModel createRequestModel(String path, RequestMethod method) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ems-me-hardware-id", deviceInfo.getHwid());
        headers.put("x-ems-me-application-code", requestContext.getApplicationCode());
        headers.put("x-ems-me-contact-field-id", String.valueOf(appLoginParameters_withCredentials.getContactFieldId()));
        headers.put("x-ems-me-contact-field-value", appLoginParameters_withCredentials.getContactFieldValue());
        headers.putAll(RequestHeaderUtils_Old.createDefaultHeaders(requestContext));
        headers.putAll(RequestHeaderUtils_Old.createBaseHeaders_V2(requestContext));

        return new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(path)
                .headers(headers)
                .method(method)
                .build();
    }

    private List<Notification> createNotificationList() throws JSONException {
        Map<String, String> customData1 = new HashMap<>();
        customData1.put("data1", "dataValue1");
        customData1.put("data2", "dataValue2");

        JSONObject rootParams1 = new JSONObject();
        rootParams1.put("param1", "paramValue1");
        rootParams1.put("param2", "paramValue2");

        Map<String, String> customData2 = new HashMap<>();
        customData2.put("data3", "dataValue3");
        customData2.put("data4", "dataValue4");

        JSONObject rootParams2 = new JSONObject();
        rootParams2.put("param3", "paramValue3");
        rootParams2.put("param4", "paramValue4");


        Map<String, String> customData3 = new HashMap<>();
        customData3.put("data5", "dataValue5");
        customData3.put("data6", "dataValue6");

        JSONObject rootParams3 = new JSONObject();
        rootParams3.put("param5", "paramValue5");
        rootParams3.put("param6", "paramValue6");

        return Arrays.asList(
                new Notification("id1", "sid1", "title1", null, customData1, rootParams1, 300, 10000000),
                new Notification("id2", "sid2", "title2", null, customData2, rootParams2, 200, 30000000),
                new Notification("id3", "sid3", "title3", null, customData3, rootParams3, 100, 25000000)

        );
    }

    private List<Notification> createCacheList() throws JSONException {
        Map<String, String> customData4 = new HashMap<>();
        customData4.put("data7", "dataValue7");
        customData4.put("data8", "dataValue8");

        JSONObject rootParams4 = new JSONObject();
        rootParams4.put("param7", "paramValue7");
        rootParams4.put("param8", "paramValue8");

        Map<String, String> customData5 = new HashMap<>();
        customData5.put("data9", "dataValue9");
        customData5.put("data10", "dataValue10");

        JSONObject rootParams5 = new JSONObject();
        rootParams5.put("param9", "paramValue9");
        rootParams5.put("param10", "paramValue10");

        return Arrays.asList(
                new Notification("id4", "sid4", "title4", null, customData4, rootParams4, 400, 40000000),
                new Notification("id5", "sid5", "title5", null, customData5, rootParams5, 500, 50000000)
        );
    }

    private ResponseModel createSuccessResponse() {
        String notificationString1 = "{" +
                "\"id\":\"id1\", " +
                "\"sid\":\"sid1\", " +
                "\"title\":\"title1\", " +
                "\"custom_data\": {" +
                "\"data1\":\"dataValue1\"," +
                "\"data2\":\"dataValue2\"" +
                "}," +
                "\"root_params\": {" +
                "\"param1\":\"paramValue1\"," +
                "\"param2\":\"paramValue2\"" +
                "}," +
                "\"expiration_time\": 300, " +
                "\"received_at\":10000000" +
                "}";

        String notificationString2 = "{" +
                "\"id\":\"id2\", " +
                "\"sid\":\"sid2\", " +
                "\"title\":\"title2\", " +
                "\"custom_data\": {" +
                "\"data3\":\"dataValue3\"," +
                "\"data4\":\"dataValue4\"" +
                "}," +
                "\"root_params\": {" +
                "\"param3\":\"paramValue3\"," +
                "\"param4\":\"paramValue4\"" +
                "}," +
                "\"expiration_time\": 200, " +
                "\"received_at\":30000000" +
                "}";

        String notificationString3 = "{" +
                "\"id\":\"id3\", " +
                "\"sid\":\"sid3\", " +
                "\"title\":\"title3\", " +
                "\"custom_data\": {" +
                "\"data5\":\"dataValue5\"," +
                "\"data6\":\"dataValue6\"" +
                "}," +
                "\"root_params\": {" +
                "\"param5\":\"paramValue5\"," +
                "\"param6\":\"paramValue6\"" +
                "}," +
                "\"expiration_time\": 100, " +
                "\"received_at\":25000000" +
                "}";

        String json = "{\"badge_count\": 300, \"notifications\": [" + notificationString1 + "," + notificationString2 + "," + notificationString3 + "]}";

        return new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(json)
                .requestModel(mock(RequestModel.class))
                .build();
    }

    @SuppressWarnings("unchecked")
    private RequestManager requestManagerWithRestClient(RestClient restClient) {
        return new RequestManager(
                mock(Handler.class),
                mock(Repository.class),
                mock(Repository.class),
                mock(Worker.class),
                restClient,
                mock(Registry.class),
                mock(CompletionProxyFactory.class));
    }
}