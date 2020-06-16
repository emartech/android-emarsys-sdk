package com.emarsys.mobileengage.notification.command

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.test.rule.ActivityTestRule
import com.emarsys.mobileengage.fake.FakeActivityLifecycleCallbacks
import com.emarsys.mobileengage.notification.LaunchActivityCommandLifecycleCallbacksFactory
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.fake.FakeActivity
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch

class LaunchApplicationCommandTest {

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    lateinit var mockProviderLaunchActivityCommand: LaunchActivityCommandLifecycleCallbacksFactory

    @Before
    fun setUp() {
        mockProviderLaunchActivityCommand = Mockito.mock(LaunchActivityCommandLifecycleCallbacksFactory::class.java)
    }

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_intentMustNotBeNull() {
        LaunchApplicationCommand(null, getTargetContext().applicationContext, mockProviderLaunchActivityCommand)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_contextMustNotBeNull() {
        LaunchApplicationCommand(Intent(), null, mockProviderLaunchActivityCommand)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_providerMustNotBeNull() {
        LaunchApplicationCommand(Intent(), getTargetContext().applicationContext, null)
    }

    @Test
    fun testRun_startsActivity_withCorrectIntent() {
        val captor = ArgumentCaptor.forClass(Intent::class.java)
        val launchIntentForPackage = Intent()
        val pm: PackageManager = mock()
        whenever(pm.getLaunchIntentForPackage(ArgumentMatchers.anyString())).thenReturn(launchIntentForPackage)
        val mockActivity: Activity = mock()
        whenever(mockActivity.applicationContext).thenReturn(mock<Application>())
        whenever(mockActivity.packageManager).thenReturn(pm)
        whenever(mockActivity.packageName).thenReturn("packageName")
        val extras = Bundle()
        extras.putLong("key1", 800)
        extras.putString("key2", "value")
        val remoteIntent = Intent()
        remoteIntent.putExtras(extras)
        val command: Runnable = LaunchApplicationCommand(remoteIntent, mockActivity, mockProviderLaunchActivityCommand)
        command.run()
        verify(mockActivity).startActivity(captor.capture())
        val expectedBundle = launchIntentForPackage.extras
        val resultBundle = captor.value.extras
        resultBundle!!.keySet() shouldBe expectedBundle!!.keySet()

        for (key in expectedBundle.keySet()) {
            resultBundle[key] shouldBe expectedBundle[key]
        }
    }

    @Test
    fun testRun_startsActivity_withIncorrectIntent() {
        val pm: PackageManager = mock()
        val mockActivity: Activity = mock()
        whenever(mockActivity.packageManager).thenReturn(pm)
        whenever(mockActivity.packageName).thenReturn("packageName")
        whenever(mockActivity.applicationContext).thenReturn(mock<Application>())

        val extras = Bundle()
        extras.putLong("key1", 800)
        extras.putString("key2", "value")
        val remoteIntent = Intent()
        remoteIntent.putExtras(extras)
        val command: Runnable = LaunchApplicationCommand(remoteIntent, mockActivity, mockProviderLaunchActivityCommand)
        command.run()
    }

    @Test
    fun testLaunchActivity_shouldBlock() {
        whenever(mockProviderLaunchActivityCommand.create(any())).thenAnswer { invocation ->
            FakeActivityLifecycleCallbacks(onResume = { (invocation.getArgument(0) as CountDownLatch).countDown() })
        }

        val fakeActivity = activityRule.activity
        val command: Runnable = LaunchApplicationCommand(Intent(), fakeActivity, mockProviderLaunchActivityCommand)

        command.run()

        fakeActivity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) shouldBe true
    }
}