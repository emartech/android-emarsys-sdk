package com.emarsys.mobileengage.notification.command;

import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.testUtil.ExperimentalTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class TrackActionClickCommandTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
    }

    @After
    public void tearDown() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternal_mustNotBeNull() {
        new TrackActionClickCommand(null, "", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonId_mustNotBeNull() {
        new TrackActionClickCommand(mock(MobileEngageInternal.class), null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_title_mustNotBeNull() {
        new TrackActionClickCommand(mock(MobileEngageInternal.class), "", null);
    }

    @Test
    public void testRun_doesNotCallMobileEngageInternal_ifV3isNotEnabled() {
        MobileEngageInternal internalMock = mock(MobileEngageInternal.class);

        new TrackActionClickCommand(internalMock, "", "").run();

        verifyZeroInteractions(internalMock);
    }

    @Test
    public void testRun_sendsInternalCustomEvent_ifInAppIsEnabled() {
        MobileEngageExperimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);

        verifyTrackInternalCustomEventIsCalled();
    }

    @Test
    public void testRun_sendsInternalCustomEvent_ifInboxV2IsEnabled() {
        MobileEngageExperimental.enableFeature(MobileEngageFeature.USER_CENTRIC_INBOX);

        verifyTrackInternalCustomEventIsCalled();
    }

    private void verifyTrackInternalCustomEventIsCalled() {
        MobileEngageInternal internalMock = mock(MobileEngageInternal.class);
        String buttonId = "buttonId";
        String title = "title";

        new TrackActionClickCommand(internalMock, buttonId, title).run();

        Map<String, String> payload = new HashMap<>();
        payload.put("button_id", buttonId);
        payload.put("title", title);
        verify(internalMock).trackInternalCustomEvent("richNotification:actionClicked", payload);
    }

}