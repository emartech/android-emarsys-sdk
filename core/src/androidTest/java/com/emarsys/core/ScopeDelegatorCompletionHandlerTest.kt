package com.emarsys.core

import com.emarsys.core.response.ResponseModel
import com.emarsys.testUtil.mockito.ThreadSpy
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import java.util.concurrent.Executors

class ScopeDelegatorCompletionHandlerTest {

    private lateinit var scopeDelegatorCompletionHandler: ScopeDelegatorCompletionHandler
    private lateinit var randomScope: CoroutineScope
    private lateinit var threadSpy: ThreadSpy<CoreCompletionHandler>

    @Before
    fun setUp() {
        val executor = Executors.newSingleThreadExecutor()
        randomScope = CoroutineScope(Job() + executor.asCoroutineDispatcher())
        threadSpy = ThreadSpy()
    }

    @Test
    fun testOnSuccess() {
        assertScope(
            on = { scopeDelegatorCompletionHandler.onSuccess("test", mock()) },
            then = {
                threadSpy.answer(it)
            })
    }

    @Test
    fun testOnError() {
        assertScope(
            on = { scopeDelegatorCompletionHandler.onError("test", mock<ResponseModel>()) },
            then = {
                threadSpy.answer(it)
            })
    }

    @Test
    fun testOnError_exception() {
        assertScope(
            on = { scopeDelegatorCompletionHandler.onError("test", mock<Exception>()) },
            then = {
                threadSpy.answer(it)
            })
    }

    private fun assertScope(then: (InvocationOnMock) -> Unit, on: () -> Unit) {
        runBlocking {
            val scopeThread = getScopeThread(randomScope)
            val mockCompletionHandler = mock<CoreCompletionHandler> {
                on { onSuccess(any(), any()) } doAnswer {
                    then(it)
                }
                on { onError(any(), any<ResponseModel>()) } doAnswer {
                    then(it)
                }
                on { onError(any(), any<Exception>()) } doAnswer {
                    then(it)
                }
            }
            scopeDelegatorCompletionHandler =
                ScopeDelegatorCompletionHandler(mockCompletionHandler, randomScope)

            Thread {
                on.invoke()
            }.start()

            threadSpy.verifyCalledOnThread(scopeThread)
        }
    }

    private suspend fun getScopeThread(scope: CoroutineScope): Thread {
        val job = scope.async {
            Thread.currentThread()
        }
        return job.await()
    }
}