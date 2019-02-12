package com.emarsys.mobileengage.device

import com.emarsys.core.device.DeviceInfo
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.storage.Storage
import com.emarsys.testUtil.SharedPrefsUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.*

class DeviceInfoStartActionTest {

    private lateinit var deviceInfoHashStorage: Storage<Int>
    private lateinit var mobileEngageInternal: MobileEngageInternal
    private lateinit var startAction: DeviceInfoStartAction
    private lateinit var deviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        deviceInfoHashStorage = mock(Storage::class.java) as Storage<Int>
        mobileEngageInternal = mock(MobileEngageInternal::class.java)
        deviceInfo = mock(DeviceInfo::class.java)

        startAction = DeviceInfoStartAction(mobileEngageInternal, deviceInfoHashStorage, deviceInfo)

    }

    @After
    fun tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageInternal_mustNotBeNull() {
        DeviceInfoStartAction(null, deviceInfoHashStorage, deviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfoHashStorage_mustNotBeNull() {
        DeviceInfoStartAction(mobileEngageInternal, null, deviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfo_mustNotBeNull() {
        DeviceInfoStartAction(mobileEngageInternal, deviceInfoHashStorage, null)
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageIsEmpty() {
        whenever(deviceInfoHashStorage.get()).thenReturn(null)
        startAction = DeviceInfoStartAction(mobileEngageInternal, deviceInfoHashStorage, deviceInfo)

        startAction.execute(null)

        verify(mobileEngageInternal).trackDeviceInfo()
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageHasChanged() {
        whenever(deviceInfoHashStorage.get()).thenReturn(42)
        whenever(deviceInfo.hash).thenReturn(43)

        startAction = DeviceInfoStartAction(mobileEngageInternal, deviceInfoHashStorage, deviceInfo)

        startAction.execute(null)

        verify(mobileEngageInternal).trackDeviceInfo()
    }

    @Test
    fun testExecute_shouldNotCallsMobileEngageInternal_whenStorageHasNotChangedAndExists() {
        whenever(deviceInfoHashStorage.get()).thenReturn(42)

        whenever(deviceInfo.hash).thenReturn(42)

        startAction = DeviceInfoStartAction(mobileEngageInternal, deviceInfoHashStorage, deviceInfo)

        startAction.execute(null)

        verifyZeroInteractions(mobileEngageInternal)
    }
}