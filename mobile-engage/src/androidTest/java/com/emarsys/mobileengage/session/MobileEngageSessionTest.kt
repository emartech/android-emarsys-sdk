package com.emarsys.mobileengage.session

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.mobileengage.event.EventServiceInternal
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Before
import org.junit.Test

class MobileEngageSessionTest {

    private companion object {
        const val SESSION_ID = "testSessionId"
    }

    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUUIDProvider: UUIDProvider
    private lateinit var mockEventServiceInternal: EventServiceInternal
    private lateinit var session: MobileEngageSession

    @Before
    fun setUp() {
        mockTimestampProvider = mock {
            on { provideTimestamp() } doReturn 3L doReturn 4L
        }
        mockUUIDProvider = mock {
            on { provideId() } doReturn SESSION_ID
        }
        mockEventServiceInternal = mock()
        session = MobileEngageSession(mockTimestampProvider, mockUUIDProvider, mockEventServiceInternal)
    }

    @Test
    fun testStartSession_createsUUIDAndTimestamp() {
        session.startSession()

        verify(mockUUIDProvider).provideId()
        verify(mockTimestampProvider).provideTimestamp()
    }

    @Test
    fun testStartSession_shouldSetSessionId() {
        session.startSession()

        session.sessionId shouldBe SESSION_ID
    }

    @Test
    fun testStartSession_reportSessionStartToEventServiceInternal_byInternalCustomEvent() {
        session.startSession()

        verify(mockEventServiceInternal).trackInternalCustomEventAsync(eq("session:start"), isNull(), anyOrNull())
    }

    @Test
    fun testEndSession_reportSessionEndToEventServiceInternal_byInternalCustomEvent() {
        session.startSession()
        session.endSession()

        verify(mockEventServiceInternal).trackInternalCustomEventAsync(eq("session:end"), eq(mapOf(
                "duration" to "1"
        )), anyOrNull())
    }

    @Test(expected = IllegalStateException::class)
    fun testEndSession_shouldResetSessionIdAndSessionStart() {
        session.startSession()
        session.endSession()

        session.endSession()

        session.sessionId shouldBe null
        verify(mockEventServiceInternal).trackInternalCustomEventAsync(eq("session:start"), isNull(), anyOrNull())

        verify(mockEventServiceInternal, times(1)).trackInternalCustomEventAsync(eq("session:end"), eq(mapOf(
                "duration" to "1"
        )), anyOrNull())
    }

    @Test
    fun testEndSession_shouldNotBeCalledWithoutStartSession() {
        val exception = shouldThrow<IllegalStateException> {
            session.endSession()
        }

        exception.message shouldBe "StartSession has to be called first!"
    }
}