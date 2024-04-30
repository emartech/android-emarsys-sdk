package com.emarsys.mapper

import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.util.RequestModelHelper
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

class MerchantIdHeaderMapperTest : AnnotationSpec() {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val MERCHANT_ID = "testMerchantId"
        const val APPLICATION_CODE = "applicationCode"
    }

    private lateinit var mockRequestContext: MobileEngageRequestContext
    private lateinit var mockRequestModelHelper: RequestModelHelper
    private lateinit var mockPredictRequestContext: PredictRequestContext

    private lateinit var merchantIdHeaderMapper: MerchantIdHeaderMapper

    @Before
    fun setUp() {
        mockRequestContext = mock {
            on { applicationCode } doReturn APPLICATION_CODE
        }
        mockRequestModelHelper = mock {
            on { isMobileEngageSetContactRequest(any()) } doReturn false
            on { isMobileEngageRefreshContactTokenRequest(any()) } doReturn false
        }

        mockPredictRequestContext = mock()

        merchantIdHeaderMapper =
            MerchantIdHeaderMapper(
                mockRequestContext,
                mockRequestModelHelper,
                mockPredictRequestContext
            )
    }

    @Test
    fun testMap_shouldAddMerchantIdHeader_whenMobileEngageSetContactRequest_andMerchantIdIsPresentInPredictRequestContext() {
        val originalRequestModel = createSetContactRequest()
        mockPredictRequestContext.stub {
            on { merchantId } doReturn MERCHANT_ID
        }
        mockRequestModelHelper.stub {
            on { isMobileEngageSetContactRequest(originalRequestModel) } doReturn true
            on { isMobileEngageRefreshContactTokenRequest(originalRequestModel) } doReturn false
        }

        val updatedRequestModel = merchantIdHeaderMapper.map(originalRequestModel)

        updatedRequestModel shouldBe createSetContactRequest(
            extraHeaders = mapOf(
                MerchantIdHeaderMapper.MERCHANT_ID_HEADER to MERCHANT_ID
            )
        )
    }

    @Test
    fun testMap_shouldAddMerchantIdHeader_whenMobileEngageRefreshContactTokenRequest_andMerchantIdIsPresentInPredictRequestContext() {
        val originalRequestModel = createRefreshContactTokenRequest()
        mockPredictRequestContext.stub {
            on { merchantId } doReturn MERCHANT_ID
        }
        mockRequestModelHelper.stub {
            on { isMobileEngageSetContactRequest(originalRequestModel) } doReturn false
            on { isMobileEngageRefreshContactTokenRequest(originalRequestModel) } doReturn true
        }

        val updatedRequestModel = merchantIdHeaderMapper.map(originalRequestModel)

        updatedRequestModel shouldBe createRefreshContactTokenRequest(
            extraHeaders = mapOf(
                MerchantIdHeaderMapper.MERCHANT_ID_HEADER to MERCHANT_ID
            )
        )
    }

    @Test
    fun testMap_whenNotSetContactOrRefreshContactTokenRequest_shouldIgnoreRequest() {
        val originalRequestModel = RequestModel(
            "https://me-client.eservice.emarsys.net/v3/notSetContactOrRefreshContactTokenRequest",
            RequestMethod.POST,
            null,
            mapOf(),
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )
        mockRequestModelHelper.stub {
            on { isMobileEngageSetContactRequest(originalRequestModel) } doReturn false
            on { isMobileEngageRefreshContactTokenRequest(originalRequestModel) } doReturn false
        }

        val updatedRequestModel = merchantIdHeaderMapper.map(originalRequestModel)

        updatedRequestModel shouldBe originalRequestModel
    }


    @Test
    fun testMap_shouldNotAddMerchantIdHeader_whenMobileEngageSetContactRequest_andMerchantIdIsMissingFromPredictRequestContext() {
        val originalRequestModel = createSetContactRequest()
        mockPredictRequestContext.stub {
            on { merchantId } doReturn null
        }
        mockRequestModelHelper.stub {
            on { isMobileEngageSetContactRequest(originalRequestModel) } doReturn true
            on { isMobileEngageRefreshContactTokenRequest(originalRequestModel) } doReturn false
        }

        val updatedRequestModel = merchantIdHeaderMapper.map(originalRequestModel)

        updatedRequestModel shouldBe originalRequestModel
    }

    @Test
    fun testMap_shouldNotAddMerchantIdHeader_whenMobileEngageRefreshContactTokenRequest_andMerchantIdIsMissingFromPredictRequestContext() {
        val originalRequestModel = createRefreshContactTokenRequest()
        mockPredictRequestContext.stub {
            on { merchantId } doReturn null
        }
        mockRequestModelHelper.stub {
            on { isMobileEngageSetContactRequest(originalRequestModel) } doReturn false
            on { isMobileEngageRefreshContactTokenRequest(originalRequestModel) } doReturn true
        }

        val updatedRequestModel = merchantIdHeaderMapper.map(originalRequestModel)

        updatedRequestModel shouldBe originalRequestModel
    }

    @Test
    fun testMap_shouldNotAddMerchantIdHeader_whenMobileEngageSetContactRequest_andMerchantIdIsEmptyInPredictRequestContext() {
        val originalRequestModel = createSetContactRequest()
        mockPredictRequestContext.stub {
            on { merchantId } doReturn ""
        }
        mockRequestModelHelper.stub {
            on { isMobileEngageSetContactRequest(originalRequestModel) } doReturn true
            on { isMobileEngageRefreshContactTokenRequest(originalRequestModel) } doReturn false
        }

        val updatedRequestModel = merchantIdHeaderMapper.map(originalRequestModel)

        updatedRequestModel shouldBe originalRequestModel
    }


    private fun createSetContactRequest(extraHeaders: Map<String, String> = mapOf()) = RequestModel(
        "https://me-client.eservice.emarsys.net/v3/apps/$APPLICATION_CODE/client/contact",
        RequestMethod.POST,
        null,
        extraHeaders,
        TIMESTAMP,
        Long.MAX_VALUE,
        REQUEST_ID
    )

    private fun createRefreshContactTokenRequest(extraHeaders: Map<String, String> = mapOf()) =
        RequestModel(
            "https://me-client.eservice.emarsys.net/v3/apps/${APPLICATION_CODE}/client/contact-token",
            RequestMethod.POST,
            null,
            extraHeaders,
            TIMESTAMP,
            Long.MAX_VALUE,
            REQUEST_ID
        )

}