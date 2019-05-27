package com.emarsys.core.notification

import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class NotificationManagerProxyTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_notificationManager_mustNotBeNull() {
        NotificationManagerProxy(null, NotificationManagerCompat.from(InstrumentationRegistry.getInstrumentation().context))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_notificationManagerCompat_mustNotBeNull() {
        NotificationManagerProxy(mock(NotificationManager::class.java), null)
    }
}