package com.emarsys.mobileengage.iam;

import android.os.Handler;

import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InAppStartActionTest {

    static {
        mock(Handler.class);
    }

    private MobileEngageInternal mobileEngageInternal;
    private InAppStartAction startAction;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();
        DatabaseTestUtils.deleteMobileEngageDatabase();
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();

        mobileEngageInternal = mock(MobileEngageInternal.class);

        startAction = new InAppStartAction(mobileEngageInternal);
    }

    @After
    public void tearDown() {
        DatabaseTestUtils.deleteCoreDatabase();
        DatabaseTestUtils.deleteCoreDatabase();
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternalMustNotBeNull() {
        new InAppStartAction(null);
    }

    @Test
    public void testExecute_callsMobileEngageInternal() {
        startAction.execute(null);

        verify(mobileEngageInternal).trackInternalCustomEvent("app:start", null);
    }

}