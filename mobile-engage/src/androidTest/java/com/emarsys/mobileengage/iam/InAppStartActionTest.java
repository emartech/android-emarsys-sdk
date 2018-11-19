package com.emarsys.mobileengage.iam;

import android.os.Handler;

import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.testUtil.MobileEngageSharedPrefsUtils;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

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
        MobileEngageSharedPrefsUtils.deleteMobileEngageSharedPrefs();

        mobileEngageInternal = mock(MobileEngageInternal.class);

        startAction = new InAppStartAction(mobileEngageInternal);
    }

    @After
    public void tearDown() {
        DatabaseTestUtils.deleteCoreDatabase();
        DatabaseTestUtils.deleteCoreDatabase();
        MobileEngageSharedPrefsUtils.deleteMobileEngageSharedPrefs();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternalMustNotBeNull() {
        new InAppStartAction(null);
    }

    @Test
    public void testExecute_callsMobileEngageInternal() {
        startAction.execute(null);

        verify(mobileEngageInternal).trackInternalCustomEvent("app:start", null, null);
    }

}