package com.emarsys.mobileengage.notification.command

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.emarsys.mobileengage.fake.FakeActivityLifecycleCallbacks
import com.emarsys.mobileengage.notification.LaunchActivityCommandLifecycleCallbacksFactory
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch

class LaunchApplicationCommandTest {


    private lateinit var scenario: ActivityScenario<FakeActivity>
    private lateinit var mockProviderLaunchActivityCommand: LaunchActivityCommandLifecycleCallbacksFactory

    @BeforeEach
    fun setUp() {
        scenario = ActivityScenario.launch(FakeActivity::class.java)
        scenario.onActivity { activity ->
            mockProviderLaunchActivityCommand =
                Mockito.mock(LaunchActivityCommandLifecycleCallbacksFactory::class.java)
        }
    }

    @AfterEach
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testConstructor_intentMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            LaunchApplicationCommand(
                null,
                getTargetContext().applicationContext,
                mockProviderLaunchActivityCommand
            )
        }
    }

    @Test
    fun testConstructor_contextMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            LaunchApplicationCommand(Intent(), null, mockProviderLaunchActivityCommand)
        }
    }

    @Test
    fun testConstructor_providerMustNotBeNull() {
        shouldThrow<IllegalArgumentException> {
            LaunchApplicationCommand(Intent(), getTargetContext().applicationContext, null)
        }
    }

    @Test
    fun testRun_startsActivity_withCorrectIntent() {
        val captor = ArgumentCaptor.forClass(Intent::class.java)
        val launchIntentForPackage = Intent()
        val pm: PackageManager = mock()
        whenever(pm.getLaunchIntentForPackage(ArgumentMatchers.anyString())).thenReturn(
            launchIntentForPackage
        )
        val mockActivity: Activity = mock()
        whenever(mockActivity.applicationContext).thenReturn(mock<Application>())
        whenever(mockActivity.packageManager).thenReturn(pm)
        whenever(mockActivity.packageName).thenReturn("packageName")
        val extras = Bundle()
        extras.putLong("key1", 800)
        extras.putString("key2", "value")
        val remoteIntent = Intent()
        remoteIntent.putExtras(extras)
        val command: Runnable =
            LaunchApplicationCommand(
                remoteIntent,
                mockActivity,
                mockProviderLaunchActivityCommand
            )
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
        val command: Runnable =
            LaunchApplicationCommand(
                remoteIntent,
                mockActivity,
                mockProviderLaunchActivityCommand
            )
        command.run()
    }

    @Test
    fun testLaunchActivity_shouldBlock() {
        whenever(mockProviderLaunchActivityCommand.create(any())).thenAnswer { invocation ->
            FakeActivityLifecycleCallbacks(onResume = { (invocation.getArgument(0) as CountDownLatch).countDown() })
        }

        scenario.onActivity { activity ->
            val command: Runnable =
                LaunchApplicationCommand(Intent(), activity, mockProviderLaunchActivityCommand)
            command.run()
        }

        scenario.onActivity { activity ->
            activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) shouldBe true
        }
    }
}