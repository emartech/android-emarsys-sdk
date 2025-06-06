package com.emarsys.mobileengage.session

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.storage.Storage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.event.EventServiceInternal
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class MobileEngageSessionTest  {

    private companion object {
        const val SESSION_ID = "testSessionId"
    }

    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var mockSessionIdHolder: SessionIdHolder
    private lateinit var session: MobileEngageSession
    private lateinit var mockContactTokenStorage: Storage<String?>
    private lateinit var mockMobileEngageRequestContext: MobileEngageRequestContext

    @Before
    fun setUp() {
        mockMobileEngageRequestContext = mock {
            on { applicationCode } doReturn "testApplicationCode"
        }
        mockContactTokenStorage = mock {
            on { get() } doReturn "testContactToken"
        }
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn 3L doReturn 4L
        }
        mockUUIDProvider = mock {
            on { provideId() } doReturn SESSION_ID
        }
        mockEventServiceInternal = mock()
        mockSessionIdHolder = mock {
            on { sessionId } doReturn SESSION_ID
        }
        session = MobileEngageSession(
            mockTimestampProvider,
            mockUUIDProvider,
            mockEventServiceInternal,
            mockSessionIdHolder,
            mockContactTokenStorage,
            mockMobileEngageRequestContext
        )
    }

    @Test
    fun testStartSession_createsUUIDAndTimestamp() {
        session.startSession {}

        verify(mockUUIDProvider).provideId()
        verify(mockTimestampProvider).provideTimestamp()
    }

    @Test
    fun testStartSession_shouldSetSessionId() {
        session.startSession {}

        verify(mockSessionIdHolder).sessionId = SESSION_ID
    }

    @Test
    fun testStartSession_doesNothing_whenContactTokenMissing() {
        whenever(mockContactTokenStorage.get()) doReturn null

        session.startSession {}

        verifyNoInteractions(mockUUIDProvider)
        verifyNoInteractions(mockTimestampProvider)
        verifyNoInteractions(mockEventServiceInternal)
    }

    @Test
    fun testStartSession_doesNothing_whenApplicationCodeIsMissing() {
        whenever(mockContactTokenStorage.get()) doReturn null
        whenever(mockMobileEngageRequestContext.applicationCode) doReturn null

        session.startSession {}

        verifyNoInteractions(mockUUIDProvider)
        verifyNoInteractions(mockTimestampProvider)
        verifyNoInteractions(mockEventServiceInternal)
    }

    @Test
    fun testStartSession_reportSessionStartToEventServiceInternal_byInternalCustomEvent() {
        session.startSession {}

        verify(mockEventServiceInternal).trackInternalCustomEventAsync(
            eq("session:start"),
            isNull(),
            anyOrNull()
        )
    }

    @Test
    fun testEndSession_reportSessionEndToEventServiceInternal_byInternalCustomEvent() {
        session.startSession {}
        session.endSession {}

        verify(mockEventServiceInternal).trackInternalCustomEventAsync(
            eq("session:end"), eq(
                mapOf(
                    "duration" to "1"
                )
            ), anyOrNull()
        )
    }

    @Test
    fun testEndSession_shouldResetSessionId() {
        session.startSession {}
        session.endSession {}

        verify(mockSessionIdHolder).sessionId = null
    }

    @Test
    fun testEndSession_shouldDoNothingWhenApplicationCodeIsMissing() {
        whenever(mockMobileEngageRequestContext.applicationCode) doReturn "testAppCode"

        session.startSession {}
        whenever(mockMobileEngageRequestContext.applicationCode) doReturn null
        session.endSession {}

        verify(mockSessionIdHolder).sessionId != null
    }
}