package com.emarsys.mobileengage.device

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.ClientServiceInternal
import com.emarsys.testUtil.SharedPrefsUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*

class DeviceInfoStartActionTest {

    private lateinit var mockDeviceInfoHashStorage: Storage<Int>
    private lateinit var mockClientServiceInternal: ClientServiceInternal
    private lateinit var startAction: DeviceInfoStartAction
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockDeviceInfoHashStorage = mock(Storage::class.java) as Storage<Int>
        mockClientServiceInternal = mock(ClientServiceInternal::class.java)
        mockDeviceInfo = mock(DeviceInfo::class.java)

        startAction = DeviceInfoStartAction(mockClientServiceInternal, mockDeviceInfoHashStorage, mockDeviceInfo)

    }

    @After
    fun tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageInternal_mustNotBeNull() {
        DeviceInfoStartAction(null, mockDeviceInfoHashStorage, mockDeviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfoHashStorage_mustNotBeNull() {
        DeviceInfoStartAction(mockClientServiceInternal, null, mockDeviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfo_mustNotBeNull() {
        DeviceInfoStartAction(mockClientServiceInternal, mockDeviceInfoHashStorage, null)
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageIsEmpty() {
        whenever(mockDeviceInfoHashStorage.get()).thenReturn(null)

        startAction.execute(null)

        verify(mockClientServiceInternal).trackDeviceInfo()
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageHasChanged() {
        whenever(mockDeviceInfoHashStorage.get()).thenReturn(42)
        whenever(mockDeviceInfo.hash).thenReturn(43)

        startAction.execute(null)

        verify(mockClientServiceInternal).trackDeviceInfo()
    }

    @Test
    fun testExecute_shouldNotCallsMobileEngageInternal_whenStorageHasNotChangedAndExists() {
        whenever(mockDeviceInfoHashStorage.get()).thenReturn(42)

        whenever(mockDeviceInfo.hash).thenReturn(42)

        startAction.execute(null)

        verifyZeroInteractions(mockClientServiceInternal)
    }
}