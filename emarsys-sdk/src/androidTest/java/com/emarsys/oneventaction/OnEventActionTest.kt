package com.emarsys.oneventaction

import android.content.Context
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.CacheableEventHandler
import com.emarsys.testUtil.IntegrationTestUtils
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OnEventActionTest {

    private lateinit var mockEventHandler: EventHandler
    private lateinit var onEventAction: OnEventAction
    private lateinit var mockOnEventActionCacheableEventHandler: CacheableEventHandler


    @BeforeEach
    fun setUp() {
        mockEventHandler = mock()
        mockOnEventActionCacheableEventHandler = mock()
        setupEmarsysComponent(
            FakeDependencyContainer(
                onEventActionCacheableEventHandler = mockOnEventActionCacheableEventHandler
            )
        )
        onEventAction = OnEventAction()
    }

    @AfterEach
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