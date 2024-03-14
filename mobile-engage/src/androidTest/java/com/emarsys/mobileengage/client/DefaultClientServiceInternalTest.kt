package com.emarsys.mobileengage.client


import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.AnnotationSpec
import com.emarsys.testUtil.mockito.whenever
import org.mockito.Mockito
import org.mockito.Mockito.verify

class DefaultClientServiceInternalTest : AnnotationSpec() {

    private lateinit var mockRequestModelFactory: MobileEngageRequestModelFactory
    private lateinit var mockRequestModel: RequestModel
    private lateinit var mockRequestManager: RequestManager
    private lateinit var clientServiceInternal: ClientServiceInternal


    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        mockRequestModel = Mockito.mock(RequestModel::class.java)

        mockRequestManager = Mockito.mock(RequestManager::class.java)
        mockRequestModelFactory = Mockito.mock(MobileEngageRequestModelFactory::class.java).apply {
            whenever(createTrackDeviceInfoRequest()).thenReturn(mockRequestModel)
        }
        clientServiceInternal =
            DefaultClientServiceInternal(mockRequestManager, mockRequestModelFactory)
    }

    @Test
    fun testTrackDeviceInfo() {
        clientServiceInternal.trackDeviceInfo(null)

        verify(mockRequestManager).submit(mockRequestModel, null)
    }

}