package com.emarsys.mobileengage.iam

import com.emarsys.core.provider.timestamp.TimestampProvider

import com.emarsys.testUtil.AnnotationSpec
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
        mockOverlayInAppPresenter = mock()
        mockTimestampProvider = mock()

        pushToInAppAction = PushToInAppAction(
            mockOverlayInAppPresenter, CAMPAIGN_ID, HTML, SID, URL, mockTimestampProvider,
            PRIORITY
        )
    }

    @Test
    fun testExecute_runsWithCorrectParams() {
        whenever(mockTimestampProvider.provideTimestamp()).doReturn(TIMESTAMP)
        pushToInAppAction.execute(mock())

        verify(mockOverlayInAppPresenter).present(CAMPAIGN_ID, SID, URL,null, TIMESTAMP, HTML,null)
    }

}