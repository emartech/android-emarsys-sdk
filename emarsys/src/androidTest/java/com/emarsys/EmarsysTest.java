package com.emarsys;

import android.support.test.runner.AndroidJUnit4;

import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.predict.PredictInternal;
import com.emarsys.testUtil.ReflectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class EmarsysTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private MobileEngageInternal mockMobileEngageInternal;
    private PredictInternal mockPredictInternal;

    @Before
    public void init() throws Exception {
        mockMobileEngageInternal = mock(MobileEngageInternal.class);
        mockPredictInternal = mock(PredictInternal.class);

        ReflectionTestUtils.setStaticField(Emarsys.class, "mobileEngageInternal", mockMobileEngageInternal);
        ReflectionTestUtils.setStaticField(Emarsys.class, "predictInternal", mockPredictInternal);
    }

    @After
    public void tearDown() throws Exception {
        ReflectionTestUtils.setStaticField(Emarsys.class, "mobileEngageInternal", null);
        ReflectionTestUtils.setStaticField(Emarsys.class, "predictInternal", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfig_config_mustNotBeNull() {
        Emarsys.setup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCustomer_customerIdMustNotBeNull() {
        Emarsys.setCustomer(null);
    }

    @Test
    public void testSetCustomer_delegatesTo_mobileEngageInternalAppLogin() {
        String customerId = "customerId";

        Emarsys.setCustomer(customerId);

        verify(mockMobileEngageInternal).appLogin(3, customerId);
    }

    @Test
    public void testSetCustomer_delegatesTo_predictInternalSetCustomer() {
        String customerId = "customerId";

        Emarsys.setCustomer(customerId);

        verify(mockPredictInternal).setCustomer(customerId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEvent_eventNameMustNotBeNull() {
        Emarsys.trackCustomEvent(null, new HashMap<String, String>());
    }
}
