package com.emarsys.inapp.ui

import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.response.ResponseModel
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory
import com.emarsys.testUtil.ReflectionTestUtils
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import org.json.JSONObject
import org.junit.After
import org.junit.Test

class InlineInAppViewTest {

    @After
    fun tearDown() {
        DependencyInjection.tearDown()
    }

    @Test
    fun testFetchInlineInAppMessage() {
        val mockRequestModel: RequestModel = mock()
        val mockRequestManager: RequestManager = mock()
        val mockRequestModelFactory: MobileEngageRequestModelFactory = mock {
            on { createFetchInlineInAppMessagesRequest("testViewId") }.doReturn(mockRequestModel)
        }

        DependencyInjection.setup(FakeDependencyContainer(requestManager = mockRequestManager, requestModelFactory = mockRequestModelFactory))

        InlineInAppView("testViewId").fetchInlineInAppMessage()

        verify(mockRequestManager).submitNow(eq(mockRequestModel), any())
    }

    @Test
    fun testFilterMessagesById() {
        val expectedBody = """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>","viewId":"main-screen-banner"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>","viewId":"main-screen-banner2"}],"oldCampaigns":[]}""".trimMargin()
        val mockResponseModel: ResponseModel = mock {
            on { body }.doReturn(expectedBody)
        }

        val inlineInAppView = InlineInAppView("main-screen-banner")
        val result = ReflectionTestUtils.invokeInstanceMethod<JSONObject>(inlineInAppView, "filterMessagesById", ResponseModel::class.java to mockResponseModel)

        val html = result.getString("html")

        html shouldBe "<html>Hello World</html>"
    }

    @Test
    fun testFilterMessagesById_whenViewId_isMissing() {
        val expectedBody = """{"inlineMessages":[
                                |{"campaignId":"765","html":"<html>Hello World</html>"},
                                |{"campaignId":"7625","html":"<html>Hello World2</html>"}],"oldCampaigns":[]}""".trimMargin()
        val mockResponseModel: ResponseModel = mock {
            on { body }.doReturn(expectedBody)
        }

        val inlineInAppView = InlineInAppView("main-screen-banner")
        val result = ReflectionTestUtils.invokeInstanceMethod<JSONObject>(inlineInAppView, "filterMessagesById", ResponseModel::class.java to mockResponseModel)

        result shouldBe null
    }

    @Test
    fun testFilterMessagesById_whenInlineMessages_isMissing() {
        val expectedBody = """{}""".trimMargin()
        val mockResponseModel: ResponseModel = mock {
            on { body }.doReturn(expectedBody)
        }

        val inlineInAppView = InlineInAppView("main-screen-banner")
        val result = ReflectionTestUtils.invokeInstanceMethod<JSONObject>(inlineInAppView, "filterMessagesById", ResponseModel::class.java to mockResponseModel)

        result shouldBe null
    }
}