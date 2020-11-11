package com.emarsys.oneventaction

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.mobileengage.api.event.EventHandler
import com.emarsys.mobileengage.event.EventHandlerProvider
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class OnEventActionTest {

    private lateinit var mockEventHandler: EventHandler
    private lateinit var onEventAction: OnEventAction
    private lateinit var mockOnEventActionEventHandlerProvider: EventHandlerProvider

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockEventHandler = mock()
        mockOnEventActionEventHandlerProvider = mock()
        DependencyInjection.setup(FakeDependencyContainer(
                onEventActionEventHandlerProvider = mockOnEventActionEventHandlerProvider)
        )
        onEventAction = OnEventAction()
    }

    @After
    fun tearDown() {
        try {
            val handler = getDependency<Handler>("coreSdkHandler")
            val looper: Looper? = handler.looper
            looper?.quit()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testOnEventAction_setsEventHandlerOnInternal() {
        onEventAction.setOnEventActionEventHandler(mockEventHandler)

        verify(mockOnEventActionEventHandlerProvider).eventHandler = mockEventHandler
    }

    @Test
    fun testSetOnEventActionEventHandlerFunction_setsEventHandlerOnInternal() {
        val eventHandlerFunction: (context: Context, eventName: String, payload: JSONObject?) -> Unit = { _, _, _ -> }
        onEventAction.setOnEventActionEventHandler(eventHandlerFunction)

        verify(mockOnEventActionEventHandlerProvider).eventHandler = any()
    }
}