package com.emarsys.mobileengage.iam.dialog.action;

import android.os.Handler;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.testUtil.mockito.ThreadSpy;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SendDisplayedIamActionTest {

    private static final String CAMPAIGN_ID = "123445";

    private SendDisplayedIamAction action;

    private Handler handler;
    private MobileEngageInternal mobileEngageInternal;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        handler = new CoreSdkHandlerProvider().provideHandler();
        mobileEngageInternal = mock(MobileEngageInternal.class);
        action = new SendDisplayedIamAction(handler, mobileEngageInternal);
    }

    @After
    public void tearDown() {
        handler.getLooper().quit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handler_mustNotBeNull() {
        new SendDisplayedIamAction(
                null,
                mock(MobileEngageInternal.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternal_mustNotBeNull() {
        new SendDisplayedIamAction(
                mock(Handler.class),
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecute_campaignIdMustNotBeNull() {
        action.execute(null);
    }

    @Test
    public void testExecute_callsRequestManager() {
        action.execute(CAMPAIGN_ID);

        String eventName = "inapp:viewed";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("message_id", CAMPAIGN_ID);

        verify(mobileEngageInternal, Mockito.timeout(500)).trackInternalCustomEvent(eventName, attributes);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecute_callsRequestManager_onCoreSdkThread() throws InterruptedException {
        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(mobileEngageInternal).trackInternalCustomEvent(any(String.class), any(Map.class));

        action.execute(CAMPAIGN_ID);

        threadSpy.verifyCalledOnCoreSdkThread();
    }


}