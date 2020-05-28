package com.emarsys.inbox

import android.os.Handler
import android.os.Looper
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.api.result.ResultListener
import com.emarsys.core.api.result.Try
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.mobileengage.api.inbox.Notification
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus
import com.emarsys.mobileengage.inbox.InboxInternal
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class InboxTest {
    private lateinit var inbox: Inbox
    private lateinit var mockInboxInternal: InboxInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockInboxInternal = mock()

        val dependencyContainer = FakeDependencyContainer(inboxInternal = mockInboxInternal)

        DependencyInjection.setup(dependencyContainer)
        inbox = Inbox()
    }

    @After
    fun tearDown() {
        try {
            val handler = getDependency<Handler>("coreSdkHandler")
            val looper: Looper? = handler.looper
            looper?.quit()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testInbox_fetchNotifications_delegatesTo_inboxInternal() {
        val resultListener: ResultListener<Try<NotificationInboxStatus>> = ResultListener<Try<NotificationInboxStatus>> { }
        inbox.fetchNotifications(resultListener)
        Mockito.verify(mockInboxInternal).fetchNotifications(resultListener)
    }

    @Test
    fun testInbox_trackNotificationOpen_delegatesTo_inboxInternal() {
        val notification = Mockito.mock(Notification::class.java)
        inbox.trackNotificationOpen(notification)
        Mockito.verify(mockInboxInternal).trackNotificationOpen(notification, null)
    }

    @Test
    fun testInbox_trackNotificationOpen_notification_resultListener_delegatesTo_inboxInternal() {
        val notification = Mockito.mock(Notification::class.java)
        val resultListener = Mockito.mock(CompletionListener::class.java)
        inbox.trackNotificationOpen(notification, resultListener)
        Mockito.verify(mockInboxInternal).trackNotificationOpen(notification, resultListener)
    }

    @Test
    fun testInbox_resetBadgeCount_delegatesTo_inboxInternal() {
        inbox.resetBadgeCount()
        Mockito.verify(mockInboxInternal).resetBadgeCount(null)
    }

    @Test
    fun testInbox_resetBadgeCount_withCompletionListener_delegatesTo_inboxInternal() {
        val mockResultListener: CompletionListener = mock()
        inbox.resetBadgeCount(mockResultListener)
        Mockito.verify(mockInboxInternal).resetBadgeCount(mockResultListener)
    }
}