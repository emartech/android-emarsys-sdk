package com.emarsys.core.worker

import android.os.Handler
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.DatabaseTestUtils.deleteCoreDatabase
import com.emarsys.core.testUtil.RequestModelTestUtils.createRequestModel

import android.os.Looper
import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.connection.ConnectionState
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.fake.FakeCompletionHandler
import com.emarsys.core.request.RestClient
import com.emarsys.core.request.factory.CompletionHandlerProxyProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.request.model.specification.QueryLatestRequestModel
import com.emarsys.core.response.ResponseModel
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.*
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.CountDownLatch

class DefaultWorkerTest {
    companion object {
        const val URL = "https://www.google.com"
    }

    private lateinit var worker: DefaultWorker
    private lateinit var watchDogMock: ConnectionWatchDog
    private lateinit var requestRepository: Repository<RequestModel, SqlSpecification>
    private lateinit var mockCoreCompletionHandler: CoreCompletionHandler
    private lateinit var mockProxyProvider: CompletionHandlerProxyProvider
    private lateinit var restClient: RestClient
    private lateinit var uiHandler: Handler
    private var now: Long = 0
    private lateinit var expiredModel1: RequestModel
    private lateinit var expiredModel2: RequestModel
    private lateinit var notExpiredModel: RequestModel

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Before
    fun setup() {
        deleteCoreDatabase()
        watchDogMock = mock()
        whenever(watchDogMock.isConnected).thenReturn(true)
        requestRepository = mock()
        mockCoreCompletionHandler = mock()
        restClient = mock()
        uiHandler = Handler(Looper.getMainLooper())
        mockProxyProvider = mock()
        worker = DefaultWorker(requestRepository, watchDogMock, uiHandler, mockCoreCompletionHandler, restClient, mockProxyProvider)
        whenever(mockProxyProvider.provideProxy(any(), any())).thenReturn(mock())
        now = System.currentTimeMillis()
        expiredModel1 = RequestModel(
                URL,
                RequestMethod.GET,
                HashMap(),
                HashMap(),
                now - 500, 300,
                "id1")
        expiredModel2 = RequestModel(
                URL,
                RequestMethod.GET,
                HashMap(),
                HashMap(),
                now - 400, 150,
                "id2")
        notExpiredModel = RequestModel(
                URL,
                RequestMethod.GET,
                HashMap(),
                HashMap(),
                now, 60000,
                "id2")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_queueShouldNotBeNull() {
        DefaultWorker(null, mock(), uiHandler, mockCoreCompletionHandler, restClient, mockProxyProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_watchDogShouldNotBeNull() {
        DefaultWorker(requestRepository, null, uiHandler, mockCoreCompletionHandler, restClient, mockProxyProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uiHandlerShouldNotBeNull() {
        DefaultWorker(requestRepository, mock(), null, mockCoreCompletionHandler, restClient, mockProxyProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_restClientShouldNotBeNull() {
        DefaultWorker(requestRepository, mock(), uiHandler, mockCoreCompletionHandler, null, mockProxyProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_proxyProvider_mustNotBeNull() {
        DefaultWorker(requestRepository, mock(), uiHandler, mockCoreCompletionHandler, restClient, null)
    }

    @Test
    fun testRun_shouldLockWorker() {
        worker = spy(worker)
        val expectedModel = createRequestModel(RequestMethod.GET)
        whenever(worker.requestRepository.query(any())).thenReturn(listOf(expectedModel))
        whenever(worker.requestRepository.isEmpty()).thenReturn(false)
        worker.unlock()
        worker.run()
        Assert.assertTrue(worker.isLocked)
    }

    @Test
    fun testConstructor_setRepositorySuccessfully() {
        worker = DefaultWorker(requestRepository, mock(), uiHandler, mockCoreCompletionHandler, restClient, mockProxyProvider)
        Assert.assertEquals(requestRepository, worker.requestRepository)
    }

    @Test
    fun testConstructor_setWatchDogSuccessfully() {
        val watchDog: ConnectionWatchDog = mock()
        worker = DefaultWorker(requestRepository, watchDog, uiHandler, mockCoreCompletionHandler, restClient, mockProxyProvider)
        Assert.assertEquals(watchDog, worker.connectionWatchDog)
    }

    @Test
    fun testConstructor_registerReceiver_called() {
        verify(watchDogMock).registerReceiver(worker)
    }

    @Test
    fun testConnectionTypeChanged_shouldCallRun_whenParameterIsConnected() {
        worker = spy(worker)
        doNothing().`when`(worker).run()
        worker.onConnectionChanged(ConnectionState.CONNECTED, true)
        verify(worker).run()
    }

    @Test
    fun testRun_executeMethodShouldBeCalledWhenConnected() {
        worker = spy(worker)
        whenever(worker.connectionWatchDog.isConnected).thenReturn(true)
        val model = createRequestModel(RequestMethod.POST)
        whenever(requestRepository.query(any())).thenReturn(listOf(model))
        whenever(requestRepository.isEmpty()).thenReturn(false)
        worker.onConnectionChanged(ConnectionState.CONNECTED, true)
        verify(worker.restClient).execute(eq(model), any())
    }

    @Test
    fun testRun_isLockedShouldBeFalse_whenThereIsNoMoreElementInTheQueue() {
        whenever(requestRepository.isEmpty()).thenReturn(true)
        worker.run()
        Assert.assertFalse(worker.isLocked)
    }

    @Test
    fun testRun_isLockedShouldBeFalse_whenNotConnectedAndIsRunning() {
        whenever(watchDogMock.isConnected).thenReturn(false)
        worker.run()
        Assert.assertFalse(worker.isLocked)
    }

    @Test
    fun testRun_queueIsNotEmptyThenSendRequestIsCalled() {
        worker = spy(worker)
        val expectedModel = createRequestModel(RequestMethod.GET)
        val captor = ArgumentCaptor.forClass(RequestModel::class.java)
        whenever(requestRepository.query(any())).thenReturn(listOf(expectedModel))
        whenever(requestRepository.isEmpty()).thenReturn(false)
        worker.run()
        verify(worker.restClient).execute(captor.capture(), any())
        val returnedModel = captor.value
        Assert.assertEquals(expectedModel, returnedModel)
    }

    @Test
    fun testRun_expiration_shouldPopExpiredRequestModels() {
        worker = spy(worker)
        whenever(requestRepository.query(any()))
                .thenReturn(listOf(expiredModel1), listOf(expiredModel2), listOf(notExpiredModel))
        whenever(requestRepository.isEmpty()).thenReturn(false, false, false, false, true)
        worker.run()
        verify(requestRepository, times(3)).query(any())
        verify(requestRepository, times(2)).remove(any())
        verify(worker.restClient).execute(eq(notExpiredModel), any())
        Assert.assertTrue(worker.isLocked)
    }

    @Test
    fun testRun_expiration_expiredRequestModelsShouldBeReportedAsError() {
        worker = spy(worker)
        val latch = CountDownLatch(2)
        worker.coreCompletionHandler = spy(FakeCompletionHandler(latch))
        whenever(requestRepository.query(any()))
                .thenReturn(listOf(expiredModel1), listOf(expiredModel2), listOf(notExpiredModel))
        whenever(requestRepository.isEmpty()).thenReturn(false, false, false, false, true)
        worker.run()
        latch.await()
        argumentCaptor<String>().apply {
            verify(worker.coreCompletionHandler, times(2)).onError(capture(), any<Exception>())
            val expectedIds: List<String> = ArrayList(listOf(expiredModel1.id, expiredModel2.id))
            Assert.assertEquals(expectedIds, allValues)
        }
    }

    @Test
    fun testRun_expiration_whenOnlyExpiredModelsWereInQueue() {
        worker = spy(worker)
        whenever(worker.requestRepository.query(any()))
                .thenReturn(listOf(expiredModel1), listOf(expiredModel2))
        whenever(worker.requestRepository.isEmpty()).thenReturn(false, false, false, true)
        worker.run()
        verify(worker.requestRepository, times(2)).query(any())
        verify(worker.requestRepository, times(2)).remove(any())
        verifyZeroInteractions(worker.restClient)
        Assert.assertTrue(worker.requestRepository.isEmpty())
        Assert.assertFalse(worker.isLocked)
    }
}