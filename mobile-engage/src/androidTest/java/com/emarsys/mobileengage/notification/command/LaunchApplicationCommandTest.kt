package com.emarsys.mobileengage.notification.command


import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.emarsys.mobileengage.fake.FakeActivityLifecycleCallbacks
import com.emarsys.mobileengage.notification.LaunchActivityCommandLifecycleCallbacksFactory
import com.emarsys.mobileengage.service.IntentUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class LaunchApplicationCommandTest  {

    private lateinit var mockLaunchActivityCommandLifecycleCallbacksFactory: LaunchActivityCommandLifecycleCallbacksFactory
    private lateinit var applicationContext: Context
    private lateinit var mockLaunchPendingIntent: PendingIntent

    @Before
    fun setUp() {
        mockLaunchActivityCommandLifecycleCallbacksFactory = mockk(relaxed = true)
        applicationContext = ApplicationProvider.getApplicationContext()
        mockLaunchPendingIntent = mockk(relaxed = true)

        mockkStatic(IntentUtils::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(IntentUtils::class)
    }

    @Test
    fun testRun_launchesApplication_withCorrectIntent() {
        val remoteIntent = Intent()
        every {
            IntentUtils.createLaunchPendingIntent(
                remoteIntent,
                applicationContext
            )
        } returns mockLaunchPendingIntent

        val command =
            LaunchApplicationCommand(
                remoteIntent,
                applicationContext,
                mockLaunchActivityCommandLifecycleCallbacksFactory
            )
        command.run()

        verify { IntentUtils.createLaunchPendingIntent(remoteIntent, applicationContext) }
        verify { mockLaunchPendingIntent.send() }
    }

    @Test
    fun testRun_shouldNotCrash_whenLaunchPendingIntentIsCancelled() {
        every {
            IntentUtils.createLaunchPendingIntent(
                any(),
                applicationContext
            )
        } returns mockLaunchPendingIntent

        every { mockLaunchPendingIntent.send() } throws PendingIntent.CanceledException()

        val command = LaunchApplicationCommand(
            Intent(),
            applicationContext,
            mockLaunchActivityCommandLifecycleCallbacksFactory
        )
        command.run()
    }

    @Test
    fun testRun_shouldNotCrash_whenInterruptedException() {
        every {
            IntentUtils.createLaunchPendingIntent(
                any(),
                applicationContext
            )
        } returns mockLaunchPendingIntent

        every { mockLaunchPendingIntent.send() } throws InterruptedException()

        val command = LaunchApplicationCommand(
            Intent(),
            applicationContext,
            mockLaunchActivityCommandLifecycleCallbacksFactory
        )
        command.run()
    }

    @Test
    fun testRun_launchActivity_shouldBlock() {
        every { mockLaunchActivityCommandLifecycleCallbacksFactory.create(any()) } answers {
            FakeActivityLifecycleCallbacks(onResume = { (it.invocation.args[0] as CountDownLatch).countDown() })
        }

        val command = LaunchApplicationCommand(
            Intent(),
            applicationContext,
            mockLaunchActivityCommandLifecycleCallbacksFactory
        )
        command.run()

        verify { mockLaunchActivityCommandLifecycleCallbacksFactory.create(any()) }
    }
}