package com.emarsys.mobileengage.device


import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import com.emarsys.testUtil.SharedPrefsUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeviceInfoStartActionTest  {

    private lateinit var deviceInfoPayloadStorage: Storage<String?>
    private lateinit var mockClientServiceInternal: ClientServiceInternal
    private lateinit var startAction: DeviceInfoStartAction
    private lateinit var mockDeviceInfo: DeviceInfo


    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        deviceInfoPayloadStorage = mockk(relaxed = true)
        mockClientServiceInternal = mockk(relaxed = true)
        mockDeviceInfo = mockk(relaxed = true)

        setupMobileEngageComponent(FakeMobileEngageDependencyContainer())

        startAction = DeviceInfoStartAction(
            mockClientServiceInternal,
            deviceInfoPayloadStorage,
            mockDeviceInfo
        )

    }

    @After
    fun tearDown() {
        SharedPrefsUtils.clearSharedPrefs("emarsys_secure_shared_preferences")
        tearDownMobileEngageComponent()
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageIsEmpty() {
        every { deviceInfoPayloadStorage.get() } returns null

        startAction.execute(null)

        waitForTask()
        verify { (mockClientServiceInternal).trackDeviceInfo(null) }
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageHasChanged() {
        every { deviceInfoPayloadStorage.get() } returns createDeviceInfoPayload()
        every { mockDeviceInfo.deviceInfoPayload } returns createOtherDeviceInfoPayload()

        startAction.execute(null)

        waitForTask()
        verify { mockClientServiceInternal.trackDeviceInfo(null) }
    }

    @Test
    fun testExecute_shouldNotCallsMobileEngageInternal_whenStorageHasNotChangedAndExists() {
        every { deviceInfoPayloadStorage.get() } returns createDeviceInfoPayload()

        every { mockDeviceInfo.deviceInfoPayload } returns createDeviceInfoPayload()

        startAction.execute(null)

        waitForTask()
        verify(exactly = 0) { mockClientServiceInternal.trackDeviceInfo(any()) }
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