package com.emarsys

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.di.DependencyInjection
import com.emarsys.di.EmarysDependencyContainer
import com.emarsys.mobileengage.api.EventHandler
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature.IN_APP_MESSAGING
import com.emarsys.mobileengage.api.experimental.MobileEngageFeature.TRACK_MESSAGE_OPEN_V3
import com.emarsys.mobileengage.storage.AppLoginStorage
import com.emarsys.mobileengage.storage.MeIdStorage
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.ExperimentalTestUtils
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.fake.FakeActivity
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch

class MobileEngageIntegrationTest {

    companion object {
        private const val APP_ID = "14C19-A121F"
        private const val APP_PASSWORD = "PaNkfOD90AVpYimMBuZopCpm8OWCrREu"
        private const val CONTACT_FIELD_ID = 3
        private const val MERCHANT_ID = "1428C8EE286EC34B"
    }

    private lateinit var latch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private var errorCause: Throwable? = null

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val activityRule = ActivityTestRule<FakeActivity>(FakeActivity::class.java)

    @Before
    fun setup() {
        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .mobileEngageCredentials(APP_ID, APP_PASSWORD)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .disableDefaultChannel()
                .build()

        setup(baseConfig)
    }

    private fun setup(config: EmarsysConfig) {
        errorCause = null
        latch = CountDownLatch(1)

        val context = InstrumentationRegistry.getTargetContext()
        ConnectionTestUtils.checkConnection(context)
        MeIdStorage(context).remove()
        AppLoginStorage(context).remove()

        ExperimentalTestUtils.resetExperimentalFeatures()

        Emarsys.setup(config)
    }

    @After
    fun tearDown() {
        ExperimentalTestUtils.resetExperimentalFeatures()

        with(DependencyInjection.getContainer<EmarysDependencyContainer>()) {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleWatchdog)
            application.unregisterActivityLifecycleCallbacks(currentActivityWatchdog)
            coreSdkHandler.looper.quit()
        }

        MeIdStorage(application).remove()
        AppLoginStorage(application).remove()

        DependencyInjection.tearDown()
    }

    @Test
    fun testSetCustomer() {
        Emarsys.setCustomer(
                "test@test.com",
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testClearCustomer() {
        Emarsys.clearCustomer(
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackCustomEvent_V2_noAttributes() {
        Emarsys.trackCustomEvent(
                "integrationTestCustomEvent",
                null,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackCustomEvent_V2_withAttributes() {
        Emarsys.trackCustomEvent(
                "integrationTestCustomEvent",
                mapOf("key1" to "value1", "key2" to "value2"),
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackCustomEvent_V3_noAttributes() {
        setupWithV3()
        IntegrationTestUtils.doAppLogin()

        Emarsys.trackCustomEvent(
                "integrationTestCustomEvent",
                null,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackCustomEvent_V3_withAttributes() {
        setupWithV3()
        IntegrationTestUtils.doAppLogin()

        Emarsys.trackCustomEvent(
                "integrationTestCustomEvent",
                mapOf("key1" to "value1", "key2" to "value2"),
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackMessageOpen_V2() {
        val intent = Intent().apply {
            putExtra("payload", Bundle().apply {
                putString("key1", "value1")
                putString("u", "{\"sid\": \"dd8_zXfDdndBNEQi\"}")
            })
        }

        Emarsys.Push.trackMessageOpen(
                intent,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testTrackMessageOpen_V3() {
        setupWithV3()
        IntegrationTestUtils.doAppLogin()

        val intent = Intent().apply {
            putExtra("payload", Bundle().apply {
                putString("key1", "value1")
                putString("u", "{\"sid\": \"dd8_zXfDdndBNEQi\"}")
            })
        }

        Emarsys.Push.trackMessageOpen(
                intent,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    @Test
    fun testDeepLinkOpen() {
        val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5_6"))

        Emarsys.trackDeepLink(
                activityRule.activity,
                intent,
                this::eventuallyStoreResult
        ).also(this::eventuallyAssertSuccess)
    }

    private fun eventuallyStoreResult(errorCause: Throwable?) {
        this.errorCause = errorCause
        latch.countDown()
    }

    private fun eventuallyAssertSuccess(ignored: Any) {
        latch.await()
        errorCause shouldBe null
    }

    private fun setupWithV3() {
        tearDown()
        EmarsysConfig.Builder()
                .from(baseConfig)
                .enableExperimentalFeatures(IN_APP_MESSAGING, TRACK_MESSAGE_OPEN_V3)
                .inAppEventHandler(mock(EventHandler::class.java))
                .build()
                .let(this::setup)
    }
}