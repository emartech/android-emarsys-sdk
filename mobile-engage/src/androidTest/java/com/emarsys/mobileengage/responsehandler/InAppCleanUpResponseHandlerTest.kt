package com.emarsys.mobileengage.responsehandler

import android.os.Looper
import com.emarsys.common.feature.InnerFeature
import com.emarsys.core.database.repository.Repository
import com.emarsys.core.database.repository.SqlSpecification
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.handler.CoreSdkHandler
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam
import com.emarsys.mobileengage.iam.model.specification.FilterByCampaignId
import com.emarsys.mobileengage.testUtil.DependencyTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.net.URL

class InAppCleanUpResponseHandlerTest {
    companion object {
        private const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        private const val EVENT_BASE = "$EVENT_HOST/v3/apps/%s/client/events"
    }

    private lateinit var handler: InAppCleanUpResponseHandler
    private lateinit var mockDisplayedIamRepository: Repository<DisplayedIam, SqlSpecification>
    private lateinit var mockButtonClickRepository: Repository<ButtonClicked, SqlSpecification>
    private lateinit var mockRequestModel: RequestModel

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockRequestModel = mock {
            on { url } doReturn URL(EVENT_BASE)
        }
        mockDisplayedIamRepository = mock()
        mockButtonClickRepository = mock()

        DependencyTestUtils.setupDependencyInjectionWithServiceProviders()

        handler = InAppCleanUpResponseHandler(mockDisplayedIamRepository, mockButtonClickRepository)

        FeatureRegistry.disableFeature(InnerFeature.EVENT_SERVICE_V4)
    }

    @After
    fun tearDown() {
        val handler = getDependency<CoreSdkHandler>()
        val looper: Looper? = handler.looper
        looper?.quit()
        DependencyInjection.tearDown()
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_parsedJsonIsNull() {
        val response = buildResponseModel(mockRequestModel, "html")
        val result = handler.shouldHandleResponse(response)

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_responseHasNotOldMessages() {
        val response = buildResponseModel(mockRequestModel, "{}")
        val result = handler.shouldHandleResponse(response)

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrueWhen_responseHasOldMessages() {
        val response = buildResponseModel(mockRequestModel)
        val result = handler.shouldHandleResponse(response)

        result shouldBe true
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhen_oldMessagesIsEmpty() {
        val response = buildResponseModel(mockRequestModel, "{'oldCampaigns': []}")
        val result = handler.shouldHandleResponse(response)

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhen_UrlIsNotCustomEventUrl() {
        val requestModel: RequestModel = mock {
            on { url } doReturn URL("https://www.emarsys.com")
        }

        val response = buildResponseModel(requestModel)
        val result = handler.shouldHandleResponse(response)

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrueWhen_UrlIsCustomEventUrl() {
        val response = buildResponseModel(mockRequestModel)
        val result = handler.shouldHandleResponse(response)

        result shouldBe true
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalseWhen_eventServiceV4_isEnabled() {
        FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)

        val response = buildResponseModel(mockRequestModel)
        val result = handler.shouldHandleResponse(response)

        result shouldBe false
    }

    @Test
    fun testHandleResponse_shouldDelete_oldInApp() {
        val response = buildResponseModel(mockRequestModel, "{'oldCampaigns': ['123']}")
        handler.handleResponse(response)
        verify(mockDisplayedIamRepository).remove(FilterByCampaignId("123"))
    }

    @Test
    fun testHandleResponse_shouldDelete_multiple_oldInApps() {
        val response = buildResponseModel(mockRequestModel)
        handler.handleResponse(response)
        verify(mockDisplayedIamRepository).remove(FilterByCampaignId("123", "456", "78910"))
    }

    @Test
    fun testHandleResponse_shouldDelete_oldButtonClick() {
        val response = buildResponseModel(mockRequestModel, "{'oldCampaigns': ['123']}")
        handler.handleResponse(response)
        verify(mockButtonClickRepository).remove(FilterByCampaignId("123"))
    }

    @Test
    fun testHandleResponse_shouldDelete_multiple_oldButtonClicks() {
        val response = buildResponseModel(mockRequestModel)
        handler.handleResponse(response)
        verify(mockButtonClickRepository).remove(FilterByCampaignId("123", "456", "78910"))
    }

    private fun buildResponseModel(requestModel: RequestModel, responseBody: String = "{'oldCampaigns': ['123', '456', '78910']}"): ResponseModel {
        return ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .requestModel(requestModel)
                .build()
    }
}