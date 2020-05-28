package com.emarsys.mobileengage.deeplink

import android.app.Activity
import android.content.Intent
import com.emarsys.core.concurrency.CoreSdkHandlerProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class DeepLinkActionTest {
    companion object {
        init {
            Mockito.mock(Intent::class.java)
            Mockito.mock(Activity::class.java)
        }
    }

    private lateinit var deepLinkInternal: DeepLinkInternal
    private lateinit var action: DeepLinkAction

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {

        DependencyInjection.setup(FakeMobileEngageDependencyContainer(coreSdkHandler = CoreSdkHandlerProvider().provideHandler()))

        deepLinkInternal = mock()
        action = DeepLinkAction(deepLinkInternal)
    }

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testExecute_callsMobileEngageInternal() {
        val intent: Intent = mock()
        val activity: Activity = mock()
        whenever(activity.intent).thenReturn(intent)

        action.execute(activity)
        waitForTask()

        verify(deepLinkInternal).trackDeepLinkOpen(activity, intent, null)
    }

    @Test
    fun testExecute_neverCallsMobileEngageInternal_whenIntentFromActivityIsNull() {
        val activity: Activity = mock()
        action.execute(activity)
        waitForTask()

        verifyZeroInteractions(deepLinkInternal)
    }

    @Test
    fun testExecute_neverCallsMobileEngageInternal_whenActivityIsNull() {
        action.execute(null)
        waitForTask()

        verifyZeroInteractions(deepLinkInternal)
    }
}