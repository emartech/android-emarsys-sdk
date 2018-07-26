package com.emarsys.mobileengage.iam.jsbridge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.webkit.WebView;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.fake.FakeActivity;
import com.emarsys.mobileengage.EventHandler;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.testUtil.mockito.ThreadSpy;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SdkSuppress(minSdkVersion = KITKAT)
public class IamJsBridgeTest {

    static {
        mock(IamDialog.class);
        mock(WebView.class);
        mock(Handler.class);
        mock(Activity.class);
    }

    private static final String CAMPAIGN_ID = "555666777";

    private IamJsBridge jsBridge;
    private EventHandler inAppEventHandler;
    private InAppMessageHandlerProvider inAppMessageHandlerProvider;
    private WebView webView;
    private Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private Handler coreSdkHandler;
    private MobileEngageInternal mobileEngageInternal;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Rule
    public ActivityTestRule<FakeActivity> activityRule = new ActivityTestRule<>(FakeActivity.class);

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        inAppEventHandler = mock(EventHandler.class);
        inAppMessageHandlerProvider = mock(InAppMessageHandlerProvider.class);
        when(inAppMessageHandlerProvider.provideHandler()).thenReturn(inAppEventHandler);

        buttonClickedRepository = mock(Repository.class);
        coreSdkHandler = new CoreSdkHandlerProvider().provideHandler();

        mobileEngageInternal = mock(MobileEngageInternal.class);

        jsBridge = new IamJsBridge(
                inAppMessageHandlerProvider,
                buttonClickedRepository,
                CAMPAIGN_ID,
                coreSdkHandler,
                mobileEngageInternal);
        webView = mock(WebView.class);
        jsBridge.setWebView(webView);
    }

    @After
    public void tearDown() throws Exception {
        coreSdkHandler.getLooper().quit();
        resetActivityWatchdog();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_messageHandlerProvider_shouldNotAcceptNull() {
        new IamJsBridge(
                null,
                buttonClickedRepository,
                CAMPAIGN_ID,
                coreSdkHandler,
                mobileEngageInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonClickedRepository_shouldNotAcceptNull() {
        new IamJsBridge(
                inAppMessageHandlerProvider,
                null,
                CAMPAIGN_ID,
                coreSdkHandler,
                mobileEngageInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_campaignId_shouldNotAcceptNull() {
        new IamJsBridge(
                inAppMessageHandlerProvider,
                buttonClickedRepository,
                null,
                coreSdkHandler,
                mobileEngageInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_coreSdkHandler_shouldNotAcceptNull() {
        new IamJsBridge(
                inAppMessageHandlerProvider,
                buttonClickedRepository,
                CAMPAIGN_ID,
                null,
                mobileEngageInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternal_shouldNotAcceptNull() {
        new IamJsBridge(
                inAppMessageHandlerProvider,
                buttonClickedRepository,
                CAMPAIGN_ID,
                coreSdkHandler,
                null);
    }

    @Test
    public void testClose_shouldInvokeCloseOnTheDialogOfTheMessageHandler() throws Exception {
        IamDialog iamDialog = initializeActivityWatchdogWithIamDialog();
        jsBridge.close("");
        verify(iamDialog, Mockito.timeout(1000)).dismiss();
    }

    @Test
    public void testClose_calledOnMainThread() throws Exception {
        IamDialog iamDialog = initializeActivityWatchdogWithIamDialog();
        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(iamDialog).dismiss();
        jsBridge.close("");

        threadSpy.verifyCalledOnMainThread();
    }

    @Test
    public void testTriggerAppEvent_shouldCallHandleApplicationEventMethodOnInAppMessageHandler() throws JSONException {
        JSONObject payload =
                new JSONObject()
                        .put("payloadKey1",
                                new JSONObject()
                                        .put("payloadKey2", "payloadValue1"));
        JSONObject json =
                new JSONObject()
                        .put("name", "eventName")
                        .put("id", "123456789")
                        .put("payload", payload);

        jsBridge.triggerAppEvent(json.toString());

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JSONObject> payloadCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(inAppEventHandler, Mockito.timeout(1000)).handleEvent(nameCaptor.capture(), payloadCaptor.capture());

        assertEquals(payload.toString(), payloadCaptor.getValue().toString());
        assertEquals("eventName", nameCaptor.getValue());
    }

    @Test
    public void testTriggerMeEvent_shouldCallMobileEngageInternal_withAttributes() throws JSONException {
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("payloadKey1", "value1");
        eventAttributes.put("payloadKey2", "value2");

        JSONObject json =
                new JSONObject()
                        .put("name", "eventName")
                        .put("id", "123456789")
                        .put("payload",
                                new JSONObject()
                                        .put("payloadKey1", "value1")
                                        .put("payloadKey2", "value2"));

        jsBridge.triggerMEEvent(json.toString());

        verify(mobileEngageInternal, Mockito.timeout(1000)).trackCustomEvent("eventName", eventAttributes);
    }

    @Test
    public void testTriggerMeEvent_shouldCallMobileEngageInternal_withoutAttributes() throws JSONException {
        JSONObject json =
                new JSONObject()
                        .put("name", "eventName")
                        .put("id", "123456789");

        jsBridge.triggerMEEvent(json.toString());

        verify(mobileEngageInternal, Mockito.timeout(1000)).trackCustomEvent("eventName", null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTriggerMeEvent_shouldCallMobileEngageInternal_onCoreSDKThread() throws JSONException, InterruptedException {
        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(mobileEngageInternal).trackCustomEvent(any(String.class), nullable(Map.class));

        String id = "12346789";
        String eventName = "eventName";
        JSONObject json = new JSONObject()
                .put("id", id)
                .put("name", eventName);

        jsBridge.triggerMEEvent(json.toString());
        threadSpy.verifyCalledOnCoreSdkThread();
    }

    @Test
    public void testTriggerMeEvent_shouldInvokeCallback_whenNameIsMissing() throws JSONException {
        String id = "12346789";
        JSONObject json = new JSONObject().put("id", id);

        jsBridge.triggerMEEvent(json.toString());

        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing name!");
        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTriggerMeEvent_shouldInvokeCallback_onSuccess() throws Exception {
        String id = "123456789";
        JSONObject json = new JSONObject().put("id", id).put("name", "value");

        String requestId = "eventId";
        when(mobileEngageInternal.trackCustomEvent(any(String.class), nullable(Map.class))).thenReturn(requestId);

        jsBridge.triggerMEEvent(json.toString());

        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", true)
                .put("meEventId", requestId);

        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testTriggerAppEvent_shouldNotThrowException_whenInAppMessageHandle_isNotSet() throws JSONException {
        JSONObject json = new JSONObject().put("name", "eventName").put("id", "123456789");

        InAppMessageHandlerProvider messageHandlerProvider = mock(InAppMessageHandlerProvider.class);
        when(messageHandlerProvider.provideHandler()).thenReturn(null);

        IamJsBridge jsBridge = new IamJsBridge(
                messageHandlerProvider,
                buttonClickedRepository,
                CAMPAIGN_ID,
                coreSdkHandler,
                mobileEngageInternal);
        jsBridge.triggerAppEvent(json.toString());
    }

    @Test
    public void testTriggerAppEvent_inAppMessageHandler_calledOnMainThread() throws JSONException, InterruptedException {
        JSONObject json = new JSONObject().put("name", "eventName").put("id", "123456789");
        ThreadSpy threadSpy = new ThreadSpy();

        EventHandler messageHandler = mock(EventHandler.class);
        doAnswer(threadSpy).when(messageHandler).handleEvent("eventName", null);

        InAppMessageHandlerProvider messageHandlerProvider = mock(InAppMessageHandlerProvider.class);
        when(messageHandlerProvider.provideHandler()).thenReturn(messageHandler);

        IamJsBridge jsBridge = new IamJsBridge(
                messageHandlerProvider,
                buttonClickedRepository,
                CAMPAIGN_ID,
                coreSdkHandler,
                mobileEngageInternal);
        jsBridge.setWebView(webView);
        jsBridge.triggerAppEvent(json.toString());

        threadSpy.verifyCalledOnMainThread();
    }

    @Test
    public void testTriggerAppEvent_shouldInvokeCallback_onSuccess() throws Exception {
        String id = "123456789";
        JSONObject json = new JSONObject().put("id", id).put("name", "value");
        jsBridge.triggerAppEvent(json.toString());

        JSONObject result = new JSONObject().put("id", id).put("success", true);

        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testTriggerAppEvent_shouldInvokeCallback_whenNameIsMissing() throws Exception {
        String id = "123456789";
        JSONObject json = new JSONObject().put("id", id);
        jsBridge.triggerAppEvent(json.toString());

        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing name!");

        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testButtonClicked_shouldStoreButtonClick_inRepository() throws Exception {
        ArgumentCaptor<ButtonClicked> buttonClickedArgumentCaptor = ArgumentCaptor.forClass(ButtonClicked.class);

        String id = "12346789";
        String buttonId = "987654321";
        JSONObject json = new JSONObject().put("id", id).put("buttonId", buttonId);

        long before = System.currentTimeMillis();
        jsBridge.buttonClicked(json.toString());

        verify(buttonClickedRepository, Mockito.timeout(1000)).add(buttonClickedArgumentCaptor.capture());
        long after = System.currentTimeMillis();
        ButtonClicked buttonClicked = buttonClickedArgumentCaptor.getValue();

        assertEquals(CAMPAIGN_ID, buttonClicked.getCampaignId());
        assertEquals(buttonId, buttonClicked.getButtonId());
        assertThat(
                buttonClicked.getTimestamp(),
                allOf(greaterThanOrEqualTo(before), lessThanOrEqualTo(after)));
    }

    @Test
    public void testButtonClicked_shouldSendInternalEvent_throughMobileEngageInternal() throws Exception {
        String id = "12346789";
        String buttonId = "987654321";
        JSONObject json = new JSONObject().put("id", id).put("buttonId", buttonId);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("message_id", CAMPAIGN_ID);
        attributes.put("button_id", buttonId);

        jsBridge.buttonClicked(json.toString());

        verify(mobileEngageInternal, Mockito.timeout(1000)).trackInternalCustomEvent("inapp:click", attributes);
    }

    @Test
    public void testButtonClicked_shouldCallAddOnRepository_onCoreSDKThread() throws JSONException, InterruptedException {
        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(buttonClickedRepository).add(any(ButtonClicked.class));

        String id = "12346789";
        String buttonId = "987654321";
        JSONObject json = new JSONObject().put("id", id).put("buttonId", buttonId);

        jsBridge.buttonClicked(json.toString());
        threadSpy.verifyCalledOnCoreSdkThread();
    }

    @Test
    public void testButtonClicked_shouldInvokeCallback_onSuccess() throws JSONException {
        String id = "12346789";
        String buttonId = "987654321";
        JSONObject json = new JSONObject().put("id", id).put("buttonId", buttonId);

        jsBridge.buttonClicked(json.toString());
        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", true);
        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testButtonClicked_shouldInvokeCallback_whenButtonIdIsMissing() throws JSONException {
        String id = "12346789";
        JSONObject json = new JSONObject().put("id", id);

        jsBridge.buttonClicked(json.toString());

        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing buttonId!");
        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testOpenExternalLink_shouldStartActivity_withViewIntent() throws Exception {
        Activity activity = mock(Activity.class);
        when(activity.getPackageManager()).thenReturn(activityRule.getActivity().getPackageManager());
        initializeActivityWatchdog(activity, true);

        String id = "12346789";
        String url = "https://emarsys.com";
        JSONObject json = new JSONObject().put("id", id).put("url", url);

        jsBridge.openExternalLink(json.toString());


        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(activity, Mockito.timeout(1000)).startActivity(captor.capture());
        Intent intent = captor.getValue();
        assertEquals(Intent.ACTION_VIEW, intent.getAction());
        assertEquals(Uri.parse(url), intent.getData());
    }

    @Test
    public void testOpenExternalLink_shouldStartActivity_onMainThread() throws Exception {
        Activity activity = mock(Activity.class);
        when(activity.getPackageManager()).thenReturn(activityRule.getActivity().getPackageManager());
        initializeActivityWatchdog(activity, true);

        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(activity).startActivity(any(Intent.class));

        String id = "12346789";
        String url = "https://emarsys.com";
        JSONObject json = new JSONObject().put("id", id).put("url", url);

        jsBridge.openExternalLink(json.toString());
        threadSpy.verifyCalledOnMainThread();
    }

    @Test
    public void testOpenExternalLink_shouldInvokeCallback_onSuccess() throws Exception {
        Activity activity = mock(Activity.class);
        when(activity.getPackageManager()).thenReturn(activityRule.getActivity().getPackageManager());
        initializeActivityWatchdog(activity, true);

        String id = "12346789";
        String url = "https://emarsys.com";
        JSONObject json = new JSONObject().put("id", id).put("url", url);

        jsBridge.openExternalLink(json.toString());

        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", true);
        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testOpenExternalLink_shouldInvokeCallback_whenUrlIsMissing() throws JSONException {
        String id = "12346789";
        JSONObject json = new JSONObject().put("id", id);

        jsBridge.openExternalLink(json.toString());

        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Missing url!");
        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testOpenExternalLink_shouldInvokeCallback_whenActivityIsNull() throws Exception {
        initializeActivityWatchdog(null, true);

        String id = "12346789";
        JSONObject json = new JSONObject().put("id", id).put("url", "https://emarsys.com");

        jsBridge.openExternalLink(json.toString());

        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "UI unavailable!");
        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testOpenExternalLink_shouldInvokeCallback_whenIntentCannotBeResoled() throws Exception {
        initializeActivityWatchdog(activityRule.getActivity(), true);

        String id = "12346789";
        JSONObject json = new JSONObject().put("id", id).put("url", "This is not a valid url!");

        jsBridge.openExternalLink(json.toString());

        JSONObject result = new JSONObject()
                .put("id", id)
                .put("success", false)
                .put("error", "Url cannot be handled by any application!");
        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendResult_whenPayloadIsNull() throws Exception {
        jsBridge.sendResult(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendResult_whenPayloadDoesntContainId() throws Exception {
        jsBridge.sendResult(new JSONObject());
    }

    @Test
    public void testSendResult_shouldInvokeEvaluateJavascript_onWebView() throws Exception {
        JSONObject json = new JSONObject().put("id", "123456789").put("key", "value");
        jsBridge.sendResult(json);

        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", json), null);
    }

    private IamDialog initializeActivityWatchdogWithIamDialog() throws Exception {
        Activity activity = mock(Activity.class, Mockito.RETURNS_DEEP_STUBS);
        IamDialog iamDialog = mock(IamDialog.class);
        when(activity.getFragmentManager().findFragmentByTag(IamDialog.TAG)).thenReturn(iamDialog);

        initializeActivityWatchdog(activity, true);

        return iamDialog;
    }

    private void initializeActivityWatchdog(Activity activity, boolean isRegistered) throws Exception {
        Field currentActivityField = CurrentActivityWatchdog.class.getDeclaredField("currentActivity");
        currentActivityField.setAccessible(true);
        currentActivityField.set(null, activity);

        Field isRegisteredField = CurrentActivityWatchdog.class.getDeclaredField("isRegistered");
        isRegisteredField.setAccessible(true);
        isRegisteredField.set(null, isRegistered);
    }

    private void resetActivityWatchdog() throws Exception {
        Method resetMethod = CurrentActivityWatchdog.class.getDeclaredMethod("reset");
        resetMethod.setAccessible(true);
        resetMethod.invoke(null);
    }
}