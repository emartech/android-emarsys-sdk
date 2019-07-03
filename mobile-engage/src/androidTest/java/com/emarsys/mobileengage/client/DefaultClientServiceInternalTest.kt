package com.emarsys.mobileengage.client

import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.request.RequestModelFactory
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.Mockito.verify

class DefaultClientServiceInternalTest {

    private lateinit var mockRequestModelFactory: RequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockRequestManager: RequestManager
    private lateinit var clientServiceInternal: ClientServiceInternal

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockRequestModel = Mockito.mock(RequestModel::class.java)

        mockRequestManager = Mockito.mock(RequestManager::class.java)
        mockRequestModelFactory = Mockito.mock(RequestModelFactory::class.java).apply {
            whenever(createTrackDeviceInfoRequest()).thenReturn(mockRequestModel)
        }
        clientServiceInternal = DefaultClientServiceInternal(mockRequestManager, mockRequestModelFactory)
    }

    @Test
    fun testTrackDeviceInfo() {
        clientServiceInternal.trackDeviceInfo()

        verify(mockRequestManager).submit(mockRequestModel, null)
    }

}