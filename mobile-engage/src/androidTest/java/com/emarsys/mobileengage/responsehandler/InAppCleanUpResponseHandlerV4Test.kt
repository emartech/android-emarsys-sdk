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
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.net.URL

class InAppCleanUpResponseHandlerV4Test {
    companion object {
        private const val EVENT_HOST = "https://mobile-events.eservice.emarsys.net"
        private const val EVENT_BASE = "$EVENT_HOST/v4/apps/%s/events"
    }

    private lateinit var handler: InAppCleanUpResponseHandlerV4
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
            on { payload } doReturn mapOf(
                    "clicks" to listOf(
                            mapOf("campaignId" to "123", "buttonId" to "123", "timestamp" to "1234"),
                            mapOf("campaignId" to "456", "buttonId" to "456", "timestamp" to "5678")),
                    "viewedMessages" to listOf(
                            mapOf("campaignId" to "78910", "buttonId" to "123123", "timestamp" to "1233214"),
                            mapOf("campaignId" to "6543", "buttonId" to "234", "timestamp" to "45321")
                    )
            )
        }
        mockDisplayedIamRepository = mock()
        mockButtonClickRepository = mock()
        DependencyTestUtils.setupDependencyInjectionWithServiceProviders()

        handler = InAppCleanUpResponseHandlerV4(mockDisplayedIamRepository, mockButtonClickRepository)

        FeatureRegistry.enableFeature(InnerFeature.EVENT_SERVICE_V4)
    }

    @After
    fun tearDown() {
        val handler = getDependency<CoreSdkHandler>()
        val looper: Looper = handler.looper
        looper.quit()
        DependencyInjection.tearDown()
        FeatureTestUtils.resetFeatures()
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenNotMobileEngageEvent() {
        whenever(mockRequestModel.url).thenReturn(URL("https://not-mobile-engage.com"))
        val response = buildResponseModel(mockRequestModel)

        val result = handler.shouldHandleResponse(response)

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenEventServiceV4_isDisabled() {
        FeatureRegistry.disableFeature(InnerFeature.EVENT_SERVICE_V4)

        val response = buildResponseModel(mockRequestModel)

        val result = handler.shouldHandleResponse(response)

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_whenResponseWasSuccessful() {
        forall(
                row(buildResponseModel(mockRequestModel, statusCode = 200), true),
                row(buildResponseModel(mockRequestModel, statusCode = 299), true),
                row(buildResponseModel(mockRequestModel, statusCode = 400), false)
        ) { input, expected ->
            val result = handler.shouldHandleResponse(input)
            result shouldBe expected
        }
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenRequestIsNullOrEmpty() {
        whenever(mockRequestModel.payload).thenReturn(emptyMap())
        val responseModel = buildResponseModel(mockRequestModel)

        val result = handler.shouldHandleResponse(responseModel)

        result shouldBe false
    }

    @Test
    fun testShouldHandleResponse_shouldReturnTrue_whenRequestContainsOnlyClicksOrViewedMessages() {
        whenever(mockRequestModel.payload).thenReturn(mapOf(
                "clicks" to listOf(
                        mapOf("campaignId" to "123", "buttonId" to "123", "timestamp" to "1234"),
                        mapOf("campaignId" to "456", "buttonId" to "456", "timestamp" to "5678")),
        ))

        val responseModel = buildResponseModel(mockRequestModel)

        val result = handler.shouldHandleResponse(responseModel)

        result shouldBe true
    }

    @Test
    fun testShouldHandleResponse_shouldReturnFalse_whenRequestContainsABody_butNotViewedMessagesOrClicks() {
        whenever(mockRequestModel.payload).thenReturn(mapOf("key" to "value"))

        val responseModel = buildResponseModel(mockRequestModel)

        val result = handler.shouldHandleResponse(responseModel)

        result shouldBe false
    }

    @Test
    fun testHandleResponse_shouldRemoveSelectedCampaignIds_fromButtonClickedRepository() {
        val responseModel = buildResponseModel(mockRequestModel)

        handler.handleResponse(responseModel)

        verify(mockButtonClickRepository).remove(FilterByCampaignId("123", "456"))
    }

    @Test
    fun testHandleResponse_shouldRemoveSelectedCampaignIds_fromDisplayedMessagesRepository() {
        val responseModel = buildResponseModel(mockRequestModel)

        handler.handleResponse(responseModel)

        verify(mockDisplayedIamRepository).remove(FilterByCampaignId("78910", "6543"))
    }

    @Test
    fun testHandleResponse_shouldNotCallRepository_whenClicksOrViewedMessagesAreEmpty() {
        whenever(mockRequestModel.payload).thenReturn(mapOf(
                "clicks" to listOf<Map<String, Any?>>(),
                "viewedMessages" to listOf()
        ))
        val responseModel = buildResponseModel(mockRequestModel)

        handler.handleResponse(responseModel)

        verifyZeroInteractions(mockButtonClickRepository)
        verifyZeroInteractions(mockDisplayedIamRepository)
    }

    private fun buildResponseModel(requestModel: RequestModel, responseBody: String = "{'oldCampaigns': ['123', '456', '78910','6543']}", statusCode: Int = 200): ResponseModel {
        return ResponseModel.Builder()
                .statusCode(statusCode)
                .message("OK")
                .body(responseBody)
                .requestModel(requestModel)
                .build()
    }
}