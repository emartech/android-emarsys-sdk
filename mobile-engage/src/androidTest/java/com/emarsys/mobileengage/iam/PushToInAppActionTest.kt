package com.emarsys.mobileengage.iam

import com.emarsys.core.provider.timestamp.TimestampProvider

import com.emarsys.testUtil.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class PushToInAppActionTest : AnnotationSpec() {

    companion object {
        const val CAMPAIGN_ID = "campaignId"
        const val HTML = "html"
        const val SID = "sid"
        const val URL = "google.com"
        const val PRIORITY = 150
        const val TIMESTAMP: Long = 255555

    }

    private lateinit var pushToInAppAction: PushToInAppAction
    private lateinit var mockOverlayInAppPresenter: OverlayInAppPresenter
    private lateinit var mockTimestampProvider: TimestampProvider


    @Before
    fun setup() {
        mockOverlayInAppPresenter = mockk(relaxed = true)
        mockTimestampProvider = mockk(relaxed = true)

        pushToInAppAction = PushToInAppAction(
            mockOverlayInAppPresenter, CAMPAIGN_ID, HTML, SID, URL, mockTimestampProvider,
            PRIORITY
        )
    }

    @Test
    fun testExecute_runsWithCorrectParams() {
        every { mockTimestampProvider.provideTimestamp() } returns TIMESTAMP
        pushToInAppAction.execute(mockk(relaxed = true))

        verify {
            mockOverlayInAppPresenter.present(
                CAMPAIGN_ID,
                SID,
                URL,
                null,
                TIMESTAMP,
                HTML,
                null
            )
        }
    }

}