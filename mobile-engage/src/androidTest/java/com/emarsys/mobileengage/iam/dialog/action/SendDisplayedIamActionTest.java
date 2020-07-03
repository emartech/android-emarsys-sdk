package com.emarsys.mobileengage.iam.dialog.action;

import android.os.Handler;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.testUtil.TimeoutUtils;
import com.emarsys.testUtil.mockito.ThreadSpy;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SendDisplayedIamActionTest {

    private static final String CAMPAIGN_ID = "123445";
    private static final String SID = "testSid";
    private static final String URL = "https://www.emarsys.com";

    private SendDisplayedIamAction action;

    private Handler handler;
    private InAppInternal inAppInternal;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        handler = new CoreSdkHandlerProvider().provideHandler();
        inAppInternal = mock(InAppInternal.class);
        action = new SendDisplayedIamAction(handler, inAppInternal);
    }

    @After
    public void tearDown() {
        handler.getLooper().quit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handler_mustNotBeNull() {
        new SendDisplayedIamAction(
                null,
                mock(InAppInternal.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inAppInternal_mustNotBeNull() {
        new SendDisplayedIamAction(
                mock(Handler.class),
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecute_campaignIdMustNotBeNull() {
        action.execute(null, SID, URL);
    }

    @Test
    public void testExecute_callsRequestManager_withProperAttributes() {
        action.execute(CAMPAIGN_ID, SID, URL);

        String eventName = "inapp:viewed";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("campaignId", CAMPAIGN_ID);
        attributes.put("sid", SID);
        attributes.put("url", URL);

        verify(inAppInternal, Mockito.timeout(500)).trackInternalCustomEventAsync(
                eventName,
                attributes,
                null);
    }

    @Test
    public void testExecute_callsRequestManager_withoutSidAndUrl_when_theyAreNull() throws Exception {
        action.execute(CAMPAIGN_ID, null, null);

        String eventName = "inapp:viewed";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("campaignId", CAMPAIGN_ID);

        verify(inAppInternal, Mockito.timeout(500)).trackInternalCustomEventAsync(eventName, attributes, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecute_callsRequestManager_onCoreSdkThread() {
        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(inAppInternal).trackInternalCustomEventAsync(
                any(String.class),
                any(Map.class),
                (CompletionListener) isNull());

        action.execute(CAMPAIGN_ID, SID, URL);

        threadSpy.verifyCalledOnCoreSdkThread();
    }


}