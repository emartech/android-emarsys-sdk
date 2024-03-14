package com.emarsys.mobileengage.device


import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.client.ClientServiceInternal
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.di.tearDownMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.mobileengage.util.waitForTask
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.SharedPrefsUtils
import com.emarsys.testUtil.mockito.whenever
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class DeviceInfoStartActionTest : AnnotationSpec() {

    private lateinit var deviceInfoPayloadStorage: Storage<String?>
    private lateinit var mockClientServiceInternal: ClientServiceInternal
    private lateinit var startAction: DeviceInfoStartAction
    private lateinit var mockDeviceInfo: DeviceInfo


    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        deviceInfoPayloadStorage = mock()
        mockClientServiceInternal = mock()
        mockDeviceInfo = mock()

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
        whenever(deviceInfoPayloadStorage.get()).thenReturn(null)

        startAction.execute(null)

        waitForTask()
        verify(mockClientServiceInternal).trackDeviceInfo(null)
    }

    @Test
    fun testExecute_callsMobileEngageInternal_whenStorageHasChanged() {
        whenever(deviceInfoPayloadStorage.get()).thenReturn(createDeviceInfoPayload())
        whenever(mockDeviceInfo.deviceInfoPayload).thenReturn(createOtherDeviceInfoPayload())

        startAction.execute(null)

        waitForTask()
        verify(mockClientServiceInternal).trackDeviceInfo(null)
    }

    @Test
    fun testExecute_shouldNotCallsMobileEngageInternal_whenStorageHasNotChangedAndExists() {
        whenever(deviceInfoPayloadStorage.get()).thenReturn(createDeviceInfoPayload())

        whenever(mockDeviceInfo.deviceInfoPayload).thenReturn(createDeviceInfoPayload())

        startAction.execute(null)

        waitForTask()
        verifyNoInteractions(mockClientServiceInternal)
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