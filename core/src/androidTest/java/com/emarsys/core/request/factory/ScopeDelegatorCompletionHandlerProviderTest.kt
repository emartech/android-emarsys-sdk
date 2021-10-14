package com.emarsys.core.request.factory

import com.emarsys.core.CoreCompletionHandler
import com.emarsys.core.ScopeDelegatorCompletionHandler
import com.emarsys.testUtil.ReflectionTestUtils
import io.kotlintest.shouldBe
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class ScopeDelegatorCompletionHandlerProviderTest {

    private lateinit var scopeDelegatorCompletionHandlerProvider: ScopeDelegatorCompletionHandlerProvider

    @Before
    fun setUp() {
        scopeDelegatorCompletionHandlerProvider = ScopeDelegatorCompletionHandlerProvider()
    }

    @Test
    fun testProvide_shouldReturn_correctCompletionHandler() {
        val mockCompletionHandler: CoreCompletionHandler = mock()
        val mockScope: CoroutineScope = mock()
        val result = scopeDelegatorCompletionHandlerProvider.provide(mockCompletionHandler, mockScope)

        result.javaClass shouldBe ScopeDelegatorCompletionHandler::class.java
        ReflectionTestUtils.getInstanceField<CoreCompletionHandler>(result, "completionHandler") shouldBe mockCompletionHandler
        ReflectionTestUtils.getInstanceField<CoroutineScope>(result, "scope") shouldBe mockScope
    }
}