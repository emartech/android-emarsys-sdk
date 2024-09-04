package com.emarsys.mobileengage.notification.command


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.emarsys.mobileengage.fake.FakeActivityLifecycleCallbacks
import com.emarsys.mobileengage.notification.LaunchActivityCommandLifecycleCallbacksFactory
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

import java.util.concurrent.CountDownLatch
class LaunchApplicationCommandTest : AnnotationSpec() {

    private lateinit var mockProviderLaunchActivityCommand: LaunchActivityCommandLifecycleCallbacksFactory
    private lateinit var mockActivity: Activity

    @Before
    fun setUp() {
        mockProviderLaunchActivityCommand = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)
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
        val captor = slot<Intent>()
        val launchIntentForPackage = Intent()
        val pm: PackageManager = mockk()
        every { pm.getLaunchIntentForPackage(any()) } returns launchIntentForPackage
        every { mockActivity.packageManager } returns pm
        every { mockActivity.packageName } returns "packageName"
        every { mockActivity.applicationContext } returns getTargetContext().applicationContext

        val extras = Bundle().apply {
            putLong("key1", 800)
            putString("key2", "value")
        }

        val remoteIntent = Intent().apply {
            putExtras(extras)
        }

        val command =
            LaunchApplicationCommand(remoteIntent, mockActivity, mockProviderLaunchActivityCommand)
        command.run()

        verify { mockActivity.startActivity(capture(captor)) }
        captor.captured.extras!!.keySet() shouldBe launchIntentForPackage.extras!!.keySet()

        for (key in launchIntentForPackage.extras!!.keySet()) {
            captor.captured.extras!!.get(key) shouldBe launchIntentForPackage.extras!!.get(key)
        }
    }

    @Test
    fun testRun_startsActivity_withIncorrectIntent() {
        val pm: PackageManager = mockk()
        every { mockActivity.packageManager } returns pm
        every { mockActivity.packageName } returns "packageName"

        val extras = Bundle().apply {
            putLong("key1", 800)
            putString("key2", "value")
        }

        val remoteIntent = Intent().apply {
            putExtras(extras)
        }

        val command = LaunchApplicationCommand(
            remoteIntent,
            getTargetContext(),
            mockProviderLaunchActivityCommand
        )
        command.run()

        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    @Test
    fun testLaunchActivity_shouldBlock() {
        every { mockProviderLaunchActivityCommand.create(any()) } answers {
            FakeActivityLifecycleCallbacks(onResume = { (it.invocation.args[0] as CountDownLatch).countDown() })
        }

        val command = LaunchApplicationCommand(
            Intent(),
            getTargetContext(),
            mockProviderLaunchActivityCommand
        )
        command.run()

        verify { mockProviderLaunchActivityCommand.create(any()) }
    }
}