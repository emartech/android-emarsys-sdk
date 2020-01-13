package com.emarsys.core.provider.hardwareid

import android.content.Context
import android.provider.Settings
import com.emarsys.core.storage.Storage
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import io.kotlintest.shouldBe
import org.junit.*
import org.junit.rules.TestRule


class HardwareIdProviderTest {

    companion object {
        private const val HARDWARE_ID = "hw_value"
        private const val FIREBASE_HARDWARE_ID = "firebase_hw_value"

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            val options: FirebaseOptions = FirebaseOptions.Builder()
                    .setApplicationId("com.emarsys.sdk")
                    .build()

            FirebaseApp.initializeApp(InstrumentationRegistry.getTargetContext(), options)
        }

        @AfterClass
        @JvmStatic
        fun afterAll() {
            FirebaseApp.clearInstancesForTest()
        }

    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var context: Context
    private lateinit var mockStorage: Storage<String>
    private lateinit var hardwareIdProvider: HardwareIdProvider
    private lateinit var mockFirebaseInstanceId: FirebaseInstanceId

    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext()
        mockStorage = mock()

        mockFirebaseInstanceId = mock {
            on { id } doReturn FIREBASE_HARDWARE_ID
        }
        hardwareIdProvider = HardwareIdProvider(context, mockFirebaseInstanceId, mockStorage)
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromStorage_ifExists() {
        whenever(mockStorage.get()).thenReturn(HARDWARE_ID)

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockStorage).get()
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromFirebase_ifNotExists() {
        val result = hardwareIdProvider.provideHardwareId()
        result shouldBe FIREBASE_HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldStore_whenEmpty() {
        hardwareIdProvider.provideHardwareId()

        verify(mockStorage).set(FIREBASE_HARDWARE_ID)
    }

    @Test
    fun testProvideHardwareId_shouldFallbackToLegacySolution_whenFirebaseInstanceId_isNotAvailable() {
        val mockContext = mock<Context>()
        val expectedHardwareId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        mockFirebaseInstanceId = mock {
            on { id } doReturn null
        }
        hardwareIdProvider = HardwareIdProvider(context, mockFirebaseInstanceId, mockStorage)

        val result = hardwareIdProvider.provideHardwareId()

        verifyZeroInteractions(mockContext)
        result shouldBe expectedHardwareId
    }
}