package com.emarsys.mobileengage

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.storage.Storage
import com.emarsys.core.util.TimestampUtils
import com.emarsys.mobileengage.MobileEngageInternal_V3_Old.MOBILEENGAGE_SDK_VERSION
import com.emarsys.mobileengage.event.applogin.AppLoginParameters
import com.emarsys.mobileengage.storage.AppLoginStorage
import com.emarsys.mobileengage.storage.MeIdSignatureStorage
import com.emarsys.mobileengage.storage.MeIdStorage
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.SharedPrefsUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class MobileEngageInternal_V3_Old_sequentialApploginsTest {

    private lateinit var coreCompletionHandler: DefaultCoreCompletionHandler
    private lateinit var expectedDefaultHeaders: Map<String, String>
    private lateinit var manager: RequestManager
    private lateinit var application: Application
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var appLoginStorage: AppLoginStorage
    private lateinit var mobileEngageInternal: MobileEngageInternal_V3_Old
    private lateinit var meIdStorage: MeIdStorage
    private lateinit var meIdSignatureStorage: MeIdSignatureStorage
    private lateinit var appLoginParameters: AppLoginParameters
    private lateinit var otherAppLoginParameters: AppLoginParameters
    private lateinit var requestContext: RequestContext
    private lateinit var timestampProvider: TimestampProvider
    private lateinit var uuidProvider: UUIDProvider
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var sharedPrefs: SharedPreferences

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    companion object {
        private const val TIMESTAMP: Long = 123
        private const val REQUEST_ID = "REQUEST_ID"
        private const val CONTACT_FIELD_ID = Integer.MAX_VALUE

        private const val APPLICATION_ID = "user"
        private const val APPLICATION_PASSWORD = "pass"
        private const val ENDPOINT_BASE_V2 = "https://push.eservice.emarsys.net/api/mobileengage/v2/"
        private const val ENDPOINT_BASE_V3 = "https://mobile-events.eservice.emarsys.net/v3/devices/"
        private const val ME_ID = "ASD123"
        private const val ENDPOINT_LOGIN = ENDPOINT_BASE_V2 + "users/login"
        private const val ENDPOINT_LAST_MOBILE_ACTIVITY = "$ENDPOINT_BASE_V3$ME_ID/events"
        private const val ME_ID_SIGNATURE = "sig"

        private const val CONTACT_FIELD_VALUE_1 = "value1"
        private const val CONTACT_FIELD_VALUE_2 = "value2"
        private const val HARDWARE_ID = "hwid"
    }

    @Before
    @Suppress("UNCHECKED_CAST")
    fun init() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")

        mockCompletionListener = mock(CompletionListener::class.java)

        manager = mock(RequestManager::class.java)
        coreCompletionHandler = mock(DefaultCoreCompletionHandler::class.java)
        application = InstrumentationRegistry.getTargetContext().applicationContext as Application
        deviceInfo = DeviceInfo(application,
                mock(HardwareIdProvider::class.java).apply {
                    whenever(this.provideHardwareId()).thenReturn(HARDWARE_ID)
                },
                mock(VersionProvider::class.java),
                mock(LanguageProvider::class.java))
        sharedPrefs = application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)

        appLoginStorage = AppLoginStorage(sharedPrefs)
        appLoginStorage.remove()

        timestampProvider = mock(TimestampProvider::class.java)
        whenever(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)
        uuidProvider = mock(UUIDProvider::class.java)
        whenever(uuidProvider.provideId()).thenReturn(REQUEST_ID)

        meIdStorage = MeIdStorage(sharedPrefs)
        meIdSignatureStorage = MeIdSignatureStorage(sharedPrefs)
        requestContext = RequestContext(
                APPLICATION_ID,
                APPLICATION_PASSWORD,
                CONTACT_FIELD_ID,
                deviceInfo,
                appLoginStorage,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider,
                uuidProvider,
                mock(Storage::class.java) as Storage<String>,
                mock(Storage::class.java) as Storage<String>,
                mock(Storage::class.java) as Storage<String>,
                mock(Storage::class.java) as Storage<String>
        )

        expectedDefaultHeaders = mapOf(
                "Authorization" to "Basic dXNlcjpwYXNz"
        )

        mobileEngageInternal = MobileEngageInternal_V3_Old(
                manager,
                Handler(Looper.getMainLooper()),
                coreCompletionHandler,
                requestContext)

        meIdStorage.set(ME_ID)
        meIdSignatureStorage.set(ME_ID_SIGNATURE)

        appLoginParameters = AppLoginParameters(3, "test@test.com")
        otherAppLoginParameters = AppLoginParameters(3, "test2@test.com")
    }

    @After
    fun tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")
    }

    private val reinstantiate = { reinstantiateMobileEngage() }
    private val dontReinstantiate = { }
    private val neverLoggedIn = { }
    private val loggedInWith1 = login(CONTACT_FIELD_VALUE_1)
    private val logInWith1 = login(CONTACT_FIELD_VALUE_1)
    private val logInWith2 = login(CONTACT_FIELD_VALUE_2)
    private val withMeId = { meIdStorage.set(ME_ID) }
    private val withoutMeId = { meIdStorage.remove() }
    private val requestModelLogin1 = { createAppLogin(CONTACT_FIELD_VALUE_1) }
    private val requestModelLogin2 = { createAppLogin(CONTACT_FIELD_VALUE_2) }
    private val lastMobileActivity1 = { createLastMobileActivity() }

    @Test
    fun testSequentialAppLogins() {
        forall(
                row("1", dontReinstantiate, neverLoggedIn, logInWith1, withoutMeId, requestModelLogin1),
                row("2", dontReinstantiate, neverLoggedIn, logInWith1, withMeId, requestModelLogin1),
                row("3", dontReinstantiate, loggedInWith1, logInWith1, withoutMeId, requestModelLogin1),
                row("4", dontReinstantiate, loggedInWith1, logInWith1, withMeId, lastMobileActivity1),
                row("5", dontReinstantiate, loggedInWith1, logInWith2, withoutMeId, requestModelLogin2),
                row("6", dontReinstantiate, loggedInWith1, logInWith2, withMeId, requestModelLogin2),
                row("7", reinstantiate, neverLoggedIn, logInWith1, withoutMeId, requestModelLogin1),
                row("8", reinstantiate, neverLoggedIn, logInWith1, withMeId, requestModelLogin1),
                row("9", reinstantiate, loggedInWith1, logInWith1, withoutMeId, requestModelLogin1),
                row("10", reinstantiate, loggedInWith1, logInWith1, withMeId, lastMobileActivity1),
                row("11", reinstantiate, loggedInWith1, logInWith2, withoutMeId, requestModelLogin2),
                row("12", reinstantiate, loggedInWith1, logInWith2, withMeId, requestModelLogin2)
        ) { _, reinstantiateIfNeeded, firstLogin, secondLogin, meId, expectedRequestModel ->
            init()
            firstLogin()
            reset(manager)
            meId()

            reinstantiateIfNeeded()

            secondLogin()

            val captor = ArgumentCaptor.forClass(RequestModel::class.java)
            verify(manager).submit(captor.capture(), isNull())
            val expected = expectedRequestModel()
            val actual = captor.value
            actual shouldBe expected
        }
    }

    private fun reinstantiateMobileEngage() {
        mobileEngageInternal = MobileEngageInternal_V3_Old(
                manager,
                mock(Handler::class.java),
                coreCompletionHandler,
                requestContext)
    }

    private fun login(contactFieldValue: String) = {
        mobileEngageInternal.setContact(contactFieldValue, null)
    }

    private fun createAppLogin(contactFieldValue: String) =
            RequestModel.Builder(timestampProvider, uuidProvider)
                    .url(ENDPOINT_LOGIN)
                    .payload(mapOf(
                            "application_id" to APPLICATION_ID,
                            "hardware_id" to deviceInfo.hwid,
                            "contact_field_id" to CONTACT_FIELD_ID,
                            "contact_field_value" to contactFieldValue,
                            "platform" to deviceInfo.platform,
                            "language" to deviceInfo.language,
                            "timezone" to deviceInfo.timezone,
                            "device_model" to deviceInfo.model,
                            "application_version" to deviceInfo.applicationVersion,
                            "os_version" to deviceInfo.osVersion,
                            "ems_sdk" to MOBILEENGAGE_SDK_VERSION,
                            "push_token" to (mobileEngageInternal.pushToken ?: false)
                    ))
                    .headers(expectedDefaultHeaders)
                    .build()

    private fun createLastMobileActivity() =
            RequestModel.Builder(timestampProvider, uuidProvider)
                    .url(ENDPOINT_LAST_MOBILE_ACTIVITY)
                    .payload(mapOf(
                            "clicks" to emptyList<String>(),
                            "viewed_messages" to emptyList<String>(),
                            "events" to listOf(mapOf(
                                    "type" to "internal",
                                    "name" to "last_mobile_activity",
                                    "timestamp" to TimestampUtils.formatTimestampWithUTC(timestampProvider.provideTimestamp())
                            ))
                    ))
                    .headers(mapOf(
                            "X-ME-ID" to requestContext.meIdStorage.get(),
                            "X-ME-ID-SIGNATURE" to requestContext.meIdSignatureStorage.get(),
                            "X-ME-APPLICATIONCODE" to requestContext.applicationCode
                    ))
                    .build()


}