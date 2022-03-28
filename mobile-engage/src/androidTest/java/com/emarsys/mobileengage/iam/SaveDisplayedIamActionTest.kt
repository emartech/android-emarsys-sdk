package com.emarsys.mobileengage.iam

import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.handler.ConcurrentHandlerHolder
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.mockito.ThreadSpy
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify

class SaveDisplayedIamActionTest {
    companion object {
        private const val CAMPAIGN_ID = "123"
        private const val SID = "testSid"
        private const val URL = "https://www.emarsys.com"
        private const val TIMESTAMP: Long = 123
        private val IAM = DisplayedIam(CAMPAIGN_ID, TIMESTAMP)
    }

    private lateinit var action: SaveDisplayedIamAction
    private lateinit var repository: Repository<DisplayedIam, SqlSpecification>
    private lateinit var threadSpy: ThreadSpy<*>
    private lateinit var handler: ConcurrentHandlerHolder
    private lateinit var timestampProvider: TimestampProvider

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun init() {
        runBlocking {
            threadSpy = ThreadSpy<Any?>()
            repository = mock()
            handler =
                ConcurrentHandlerHolderFactory.create()
            timestampProvider = mock {
                on { provideTimestamp() } doReturn TIMESTAMP
            }

            org.mockito.Mockito.doAnswer(threadSpy).`when`(repository)?.add(IAM)
            action = SaveDisplayedIamAction(handler, repository, timestampProvider)
        }
    }

    @After
    fun tearDown() {
        handler.coreLooper.quit()
    }

    @Test
    fun testExecute_callsRepository() {
        action.execute(CAMPAIGN_ID, SID, URL)
        runBlocking {
            verify(repository, timeout(1000)).add(IAM)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testExecute_callsRepository_onCoreSdkThread() {
        action.execute(CAMPAIGN_ID, SID, URL)
        threadSpy.verifyCalledOnCoreSdkThread()
    }
}