package com.emarsys.predict.request

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class PredictRequestContextTest {
    private companion object {
        const val MERCHANT_ID = "merchantId"
    }

    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockTimeStampProvider: TimestampProvider
    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var mockKeyValueStore: KeyValueStore

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockDeviceInfo = mock(DeviceInfo::class.java)
        mockTimeStampProvider = mock(TimestampProvider::class.java)
        mockUUIDProvider = mock(UUIDProvider::class.java)
        mockKeyValueStore = mock(KeyValueStore::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_merchantId_mustNotBeNull() {
        PredictRequestContext(null, mockDeviceInfo, mockTimeStampProvider, mockUUIDProvider, mockKeyValueStore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfo_mustNotBeNull() {
        PredictRequestContext(MERCHANT_ID, null, mockTimeStampProvider, mockUUIDProvider, mockKeyValueStore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timeStampProvider_mustNotBeNull() {
        PredictRequestContext(MERCHANT_ID, mockDeviceInfo, null, mockUUIDProvider, mockKeyValueStore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uuidProvider_mustNotBeNull() {
        PredictRequestContext(MERCHANT_ID, mockDeviceInfo, mockTimeStampProvider, null, mockKeyValueStore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_keyValueStore_mustNotBeNull() {
        PredictRequestContext(MERCHANT_ID, mockDeviceInfo, mockTimeStampProvider, mockUUIDProvider, null)
    }

}