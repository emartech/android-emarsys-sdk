package com.emarsys.core.util

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.clientid.ClientIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.testUtil.InstrumentationRegistry.Companion.getTargetContext
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.text.SimpleDateFormat
import java.util.TimeZone

class TimestampUtilsTest  {

    companion object {
        private const val CLIENT_ID = "clientId"
        private const val SDK_VERSION = "sdkVersion"
        private const val LANGUAGE = "en-US"
    }

    private lateinit var mockClientIdProvider: ClientIdProvider
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockVersionProvider: VersionProvider
    private lateinit var mockNotificationManagerHelper: NotificationManagerHelper


    @Before
    fun setup() {
        mockClientIdProvider = mock {
            on { provideClientId() } doReturn CLIENT_ID
        }
        mockLanguageProvider = mock {
            on { provideLanguage(any()) } doReturn LANGUAGE
        }
        mockVersionProvider = mock {
            on { provideSdkVersion() } doReturn SDK_VERSION
        }
        mockNotificationManagerHelper = mock()
    }

    @Test
    fun testFormatTimestampWithUTC() {
        val deviceTimeZone = DeviceInfo(
            getTargetContext(),
            mockClientIdProvider,
            mockVersionProvider,
            mockLanguageProvider,
            mockNotificationManagerHelper,
            isAutomaticPushSendingEnabled = true,
            isGooglePlayAvailable = true
        ).timezone
        val dateString = "2017-12-07T10:46:09.100"
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        parser.timeZone = TimeZone.getTimeZone(deviceTimeZone)
        val date = parser.parse(dateString)
        val timestamp = date!!.time
        TimestampUtils.formatTimestampWithUTC(timestamp) shouldBe "2017-12-07T10:46:09.100Z"
    }
}