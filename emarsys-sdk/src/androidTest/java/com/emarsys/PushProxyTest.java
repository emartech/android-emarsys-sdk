package com.emarsys;

import android.content.Intent;

import com.emarsys.core.RunnerProxy;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.mobileengage.push.PushInternal;
import com.emarsys.push.PushProxy;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PushProxyTest {

    private RunnerProxy runnerProxy;
    private PushInternal mockPushInternal;
    private CompletionListener mockCompletionListener;
    private PushProxy pushProxy;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        runnerProxy = new RunnerProxy();
        mockPushInternal = mock(PushInternal.class);
        mockCompletionListener = mock(CompletionListener.class);

        pushProxy = new PushProxy(runnerProxy, mockPushInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_runnerProxy_mustNotBeNull() {
        new PushProxy(null, mockPushInternal);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_pushInternal_mustNotBeNull() {
        new PushProxy(runnerProxy, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_trackMessageOpen_intent_mustNotBeNull() {
        pushProxy.trackMessageOpen(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_trackMessageOpenWithCompletionListener_intent_mustNotBeNull() {
        pushProxy.trackMessageOpen(null, mockCompletionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_trackMessageOpenWithCompletionListener_completionListener_mustNotBeNull() {
        pushProxy.trackMessageOpen(mock(Intent.class), null);
    }

    @Test
    public void testPush_trackMessageOpen_delegatesTo_mobileEngageInternal() {
        Intent intent = mock(Intent.class);

        pushProxy.trackMessageOpen(intent);

        verify(mockPushInternal).trackMessageOpen(intent, null);
    }

    @Test
    public void testPush_trackMessageOpenWithCompletionListener_delegatesTo_mobileEngageInternal() {
        Intent intent = mock(Intent.class);

        pushProxy.trackMessageOpen(intent, mockCompletionListener);

        verify(mockPushInternal).trackMessageOpen(intent, mockCompletionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_setPushToken_token_mustNotBeNull() {
        pushProxy.setPushToken(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_setPushTokenWithCompletionListener_token_mustNotBeNull() {
        pushProxy.setPushToken(null, mockCompletionListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_setPushTokenWithCompletionListener_completionListener_mustNotBeNull() {
        pushProxy.setPushToken("PushToken", null);
    }

    @Test
    public void testPush_setPushToken_delegatesTo_mobileEngageInternal() {
        String PushToken = "PushToken";

        pushProxy.setPushToken(PushToken);

        verify(mockPushInternal).setPushToken(PushToken, null);
    }

    @Test
    public void testPush_setPushToken_completionListener_delegatesTo_mobileEngageInternal() {
        String PushToken = "PushToken";

        pushProxy.setPushToken(PushToken, mockCompletionListener);

        verify(mockPushInternal).setPushToken(PushToken, mockCompletionListener);
    }

    @Test
    public void testPush_removePushToken_delegatesTo_mobileEngageInternal() {
        pushProxy.clearPushToken();

        verify(mockPushInternal).clearPushToken(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPush_removePushTokenWithCompletionListener_completionListener_mustNotBeNull() {
        pushProxy.clearPushToken(null);
    }

    @Test
    public void testPush_removePushTokenWithCompletionListener_delegatesTo_mobileEngageInternal() {
        pushProxy.clearPushToken(mockCompletionListener);

        verify(mockPushInternal).clearPushToken(mockCompletionListener);
    }
}