package com.emarsys.mobileengage.iam

import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.SharedPrefsUtils
import com.emarsys.testUtil.TimeoutUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class InAppStartActionTest {

    companion object {
        private val EMARSYS_SHARED_PREFERENCES = "emarsys_shared_preferences"
    }

    private lateinit var mobileEngageInternal: MobileEngageInternal
    private lateinit var startAction: InAppStartAction

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        DatabaseTestUtils.deleteCoreDatabase()
        SharedPrefsUtils.clearSharedPrefs(EMARSYS_SHARED_PREFERENCES)

        mobileEngageInternal = mock(MobileEngageInternal::class.java)

        startAction = InAppStartAction(mobileEngageInternal)
    }

    @After
    fun tearDown() {
        DatabaseTestUtils.deleteCoreDatabase()
        DatabaseTestUtils.deleteCoreDatabase()
        SharedPrefsUtils.clearSharedPrefs(EMARSYS_SHARED_PREFERENCES)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageInternal_MustNotBeNull() {
        InAppStartAction(null)
    }

    @Test
    fun testExecute_callsMobileEngageInternal() {
        startAction.execute(null)

        verify<MobileEngageInternal>(mobileEngageInternal).trackInternalCustomEvent("app:start", null, null)
    }
}