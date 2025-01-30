package com.emarsys.mobileengage.iam.dialog.action


import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory.create
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.mobileengage.iam.InAppInternal
import com.emarsys.testUtil.mockito.ThreadSpy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever

class SendDisplayedIamActionTest  {
    private lateinit var action: SendDisplayedIamAction
    private lateinit var handler: ConcurrentHandlerHolder
    private lateinit var inAppInternal: InAppInternal

    @Before
    fun init() {
        handler = create()
        inAppInternal = Mockito.mock(InAppInternal::class.java)
        action = SendDisplayedIamAction(handler, inAppInternal)
    }

    @After
    fun tearDown() {
        handler.coreLooper.quit()
    }

    @Test
    fun testExecute_callsRequestManager_withProperAttributes() {
        action!!.execute(CAMPAIGN_ID, SID, URL)
        val eventName = "inapp:viewed"
        val attributes: MutableMap<String, String> = HashMap()
        attributes["campaignId"] = CAMPAIGN_ID
        attributes["sid"] = SID
        attributes["url"] = URL
        Mockito.verify(inAppInternal, Mockito.timeout(500)).trackInternalCustomEventAsync(
            eventName,
            attributes,
            null
        )
    }

    @Test
    @Throws(Exception::class)
    fun testExecute_callsRequestManager_withoutSidAndUrl_when_theyAreNull() {
        action.execute(CAMPAIGN_ID, null, null)
        val eventName = "inapp:viewed"
        val attributes: MutableMap<String, String> = HashMap()
        attributes["campaignId"] = CAMPAIGN_ID
        Mockito.verify(inAppInternal, Mockito.timeout(500))
            .trackInternalCustomEventAsync(eventName, attributes, null)
    }

    @Test
    fun testExecute_callsRequestManager_onCoreSdkThread() {
        val threadSpy: ThreadSpy<Any> = ThreadSpy()
        whenever(inAppInternal.trackInternalCustomEventAsync(any(), any(), isNull())) doAnswer {
            threadSpy.call()
            null
        }

        action.execute(CAMPAIGN_ID, SID, URL)
        threadSpy.verifyCalledOnCoreSdkThread()
    }

    companion object {
        private const val CAMPAIGN_ID = "123445"
        private const val SID = "testSid"
        private const val URL = "https://www.emarsys.com"
    }
}