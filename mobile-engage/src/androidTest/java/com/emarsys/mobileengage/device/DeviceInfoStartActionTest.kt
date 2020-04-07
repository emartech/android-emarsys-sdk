package com.emarsys.mobileengage.device

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.client.ClientServiceInternal
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

    private lateinit var deviceInfoPayloadStorage: Storage<String>
    private lateinit var mockClientServiceInternal: ClientServiceInternal
    private lateinit var startAction: DeviceInfoStartAction
    private lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        deviceInfoPayloadStorage = mock(Storage::class.java) as Storage<String>
        mockClientServiceInternal = mock(ClientServiceInternal::class.java)
        mockDeviceInfo = mock(DeviceInfo::class.java)

        startAction = DeviceInfoStartAction(mockClientServiceInternal, deviceInfoPayloadStorage, mockDeviceInfo)

    }

    @After
    fun tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_shared_preferences")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_mobileEngageInternal_mustNotBeNull() {
        DeviceInfoStartAction(null, deviceInfoPayloadStorage, mockDeviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfoPayloadStorage_mustNotBeNull() {
        DeviceInfoStartAction(mockClientServiceInternal, null, mockDeviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfo_mustNotBeNull() {
        DeviceInfoStartAction(mockClientServiceInternal, deviceInfoPayloadStorage, null)
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageIsEmpty() {
        whenever(deviceInfoPayloadStorage.get()).thenReturn(null)

        startAction.execute(null)

        verify(mockClientServiceInternal).trackDeviceInfo()
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageHasChanged() {
        whenever(deviceInfoPayloadStorage.get()).thenReturn(createDeviceInfoPayload())
        whenever(mockDeviceInfo.deviceInfoPayload).thenReturn(createOtherDeviceInfoPayload())

        startAction.execute(null)

        verify(mockClientServiceInternal).trackDeviceInfo()
    }

    @Test
    fun testExecute_shouldNotCallsMobileEngageInternal_whenStorageHasNotChangedAndExists() {
        whenever(deviceInfoPayloadStorage.get()).thenReturn(createDeviceInfoPayload())

        whenever(mockDeviceInfo.deviceInfoPayload).thenReturn(createDeviceInfoPayload())

        startAction.execute(null)

        verifyZeroInteractions(mockClientServiceInternal)
    }

    private fun createDeviceInfoPayload(): String {
        return """{
                  "notificationSettings": {
                    "channelSettings": [
                      [
                        {
                          "channelId": "ems_sample_news",
                          "importance": 4,
                          "isCanBypassDnd": false,
                          "isCanShowBadge": true,
                          "isShouldVibrate": false
                        },
                        {
                          "channelId": "ems_sample_messages",
                          "importance": 4,
                          "isCanBypassDnd": false,
                          "isCanShowBadge": true,
                          "isShouldVibrate": false
                        }
                      ]
                    ],
                    "importance": -1000,
                    "areNotificationsEnabled": true
                  },
                  "hwid": "1e1a57f2789e46ac",
                  "platform": "android",
                  "language": "en-US",
                  "timezone": "+0200",
                  "manufacturer": "Google",
                  "model": "Android SDK built for x86",
                  "osVersion": "10",
                  "displayMetrics": "1080x1794",
                  "sdkVersion": "2.5.0-5-gf8a8a5e"
                }"""
    }

    private fun createOtherDeviceInfoPayload(): String {
        return """{
                  "notificationSettings": {
                    "channelSettings": [
                      [
                        {
                          "channelId": "ems_sample_news",
                          "importance": 4,
                          "isCanBypassDnd": false,
                          "isCanShowBadge": true,
                          "isShouldVibrate": false
                        },
                        {
                          "channelId": "ems_sample_messages",
                          "importance": 4,
                          "isCanBypassDnd": false,
                          "isCanShowBadge": true,
                          "isShouldVibrate": false
                        }
                      ]
                    ],
                    "importance": -1000,
                    "areNotificationsEnabled": true
                  },
                  "hwid": "total_mas_hw_id",
                  "platform": "android",
                  "language": "en-US",
                  "timezone": "+0200",
                  "manufacturer": "Google",
                  "model": "Android SDK built for x86",
                  "osVersion": "10",
                  "displayMetrics": "1080x1794",
                  "sdkVersion": "2.5.0-5-gf8a8a5e"
                }"""
    }
}