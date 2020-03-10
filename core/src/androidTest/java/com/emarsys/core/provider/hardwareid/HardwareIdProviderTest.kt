package com.emarsys.core.provider.hardwareid

import android.content.Context
import android.provider.Settings
import com.emarsys.core.storage.Storage
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class HardwareIdProviderTest {

    companion object {
        private const val HARDWARE_ID = "hw_value"
    }

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    private lateinit var context: Context
    private lateinit var mockStorage: Storage<String>
    private lateinit var hardwareIdProvider: HardwareIdProvider

    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext()
        mockStorage = mock()


        hardwareIdProvider = HardwareIdProvider(context, mockStorage)
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromStorage_ifExists() {
        whenever(mockStorage.get()).thenReturn(HARDWARE_ID)

        val result = hardwareIdProvider.provideHardwareId()

        verify(mockStorage).get()
        result shouldBe HARDWARE_ID
    }

    @Test
    fun testProvideHardwareId_shouldStore_whenEmpty() {
        val expectedHardwareId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        hardwareIdProvider.provideHardwareId()

        verify(mockStorage).set(expectedHardwareId)
    }

    @Test
    fun testProvideHardwareId_shouldGetHardwareId_fromContext_ifNotFoundInStorage() {
        val expectedHardwareId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        whenever(mockStorage.get()).thenReturn(null)

        val actualHardwareId = hardwareIdProvider.provideHardwareId()

        verify(mockStorage).get()
        actualHardwareId shouldBe expectedHardwareId
    }

    @Test
    fun testProvideHardwareId_shouldSetHardwareId_inStorage_ifMissing() {
        val expectedHardwareId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        whenever(mockStorage.get()).thenReturn(null)

        hardwareIdProvider.provideHardwareId()

        Mockito.inOrder(mockStorage).apply {
            verify(mockStorage).get()
            verify(mockStorage).set(expectedHardwareId)
        }

    }

}