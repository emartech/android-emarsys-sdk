package com.emarsys.oneventaction

import android.content.Context
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.TimeoutUtils
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnEventActionTest {

    private lateinit var mockEventHandler: EventHandler
    private lateinit var onEventAction: OnEventAction
    private lateinit var mockOnEventActionCacheableEventHandler: CacheableEventHandler

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockEventHandler = mock()
        mockOnEventActionCacheableEventHandler = mock()
        setupEmarsysComponent(FakeDependencyContainer(
                onEventActionCacheableEventHandler = mockOnEventActionCacheableEventHandler)
        )
        onEventAction = OnEventAction()
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testOnEventAction_setsEventHandlerOnInternal() {
        onEventAction.setOnEventActionEventHandler(mockEventHandler)

        verify(mockOnEventActionCacheableEventHandler).setEventHandler(mockEventHandler)
    }

    @Test
    fun testSetOnEventActionEventHandlerFunction_setsEventHandlerOnInternal() {
        val eventHandlerFunction: (context: Context, eventName: String, payload: JSONObject?) -> Unit = { _, _, _ -> }
        onEventAction.setOnEventActionEventHandler(eventHandlerFunction)

        verify(mockOnEventActionCacheableEventHandler).setEventHandler(any())
    }
}