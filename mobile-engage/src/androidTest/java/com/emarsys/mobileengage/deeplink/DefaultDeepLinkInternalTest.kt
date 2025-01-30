package com.emarsys.mobileengage.deeplink

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.endpoint.ServiceEndpointProvider
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.MobileEngageRequestContext
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DefaultDeepLinkInternalTest  {
    private lateinit var mockActivity: Activity
    private lateinit var deepLinkInternal: DeepLinkInternal
    private lateinit var mockManager: RequestManager
    private lateinit var requestContext: MobileEngageRequestContext
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider
    private lateinit var mockDeviceInfo: DeviceInfo
    private lateinit var mockDeepLinkServiceProvider: ServiceEndpointProvider

    @Before
    fun init() {
        mockActivity = mockk(relaxed = true)
        mockManager = mockk(relaxed = true)
        mockTimestampProvider = mockk(relaxed = true)
        mockUuidProvider = mockk(relaxed = true)
        every { mockUuidProvider.provideId() } returns "REQUEST_ID"
        mockDeviceInfo = mockk(relaxed = true)
        every { mockDeviceInfo.sdkVersion } returns "0.0.1"
        requestContext = MobileEngageRequestContext(
            APPLICATION_CODE,
            1,
            null,
            mockDeviceInfo,
            mockTimestampProvider,
            mockUuidProvider,
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true)
        )

        mockDeepLinkServiceProvider = mockk(relaxed = true)
        every { mockDeepLinkServiceProvider.provideEndpointHost() } returns DEEPLINK_BASE
        deepLinkInternal =
            DefaultDeepLinkInternal(requestContext, mockDeepLinkServiceProvider, mockManager)
    }

    @Test
    fun testTrackDeepLink_doesNotCrashOnNonHierarchicalUris() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:a@b.com"))
        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, null)
    }

    @Test
    fun testTrackDeepLink_requestManagerCalled_withCorrectRequestModel() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5")
        )
        val payload: MutableMap<String, Any?> = HashMap()
        payload["ems_dl"] = "1_2_3_4_5"
        val headers: MutableMap<String, String> = HashMap()
        headers["User-Agent"] = String.format(
            "Emarsys SDK %s Android %s",
            requestContext.deviceInfo.sdkVersion,
            Build.VERSION.SDK_INT
        )
        val expected = RequestModel.Builder(mockTimestampProvider, mockUuidProvider)
            .url("$DEEPLINK_BASE/api/clicks")
            .headers(headers)
            .payload(payload)
            .build()

        val requestModelSlot = slot<RequestModel>()

        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, null)

        verify { mockManager.submit(capture(requestModelSlot), null) }
        assertRequestModels(expected, requestModelSlot.captured)
    }

    @Test
    fun testTrackDeepLink_requestManagerCalled_withCorrectCompletionHandler() {
        val completionListener: CompletionListener = mockk(relaxed = true)
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5")
        )

        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, completionListener)
        verify { mockManager.submit(any(), completionListener) }
    }

    @Test
    fun testTrackDeepLink_setsClickedFlag_onIntentBundle() {
        val originalIntent: Intent = mockk(relaxed = true)
        every { mockActivity.intent } returns originalIntent
        val currentIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5")
        )
        deepLinkInternal.trackDeepLinkOpen(mockActivity, currentIntent, null)
        verify { originalIntent.putExtra("ems_deep_link_tracked", true) }
    }

    @Test
    fun testTrackDeepLink_doesNotCallRequestManager_whenTrackedFlagIsSet() {
        val intentFromActivity: Intent = mockk(relaxed = true)
        every { mockActivity.intent } returns intentFromActivity
        every { intentFromActivity.getBooleanExtra("ems_deep_link_tracked", false) } returns true
        val currentIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5")
        )
        deepLinkInternal.trackDeepLinkOpen(mockActivity, currentIntent, null)
        verify(exactly = 0) { mockManager.submit(any(), any()) }
    }

    @Test
    fun testTrackDeepLink_doesNotCallRequestManager_whenDataIsNull() {
        val intent = Intent()
        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, null)
        verify(exactly = 0) { mockManager.submit(any(), any()) }
    }

    @Test
    fun testTrackDeepLink_doesNotCallRequestManager_whenUriDoesNotContainEmsDlQueryParameter() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&other=1_2_3_4_5")
        )
        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, null)
        verify(exactly = 0) { mockManager.submit(any(), any()) }
    }

    private fun assertRequestModels(expected: RequestModel, result: RequestModel) {
        result.url shouldBe expected.url
        result.method shouldBe expected.method
        result.payload shouldBe expected.payload
        result.headers shouldBe expected.headers
    }

    companion object {
        private const val APPLICATION_CODE = "applicationCode"
        private const val DEEPLINK_BASE = "https://deep-link.eservice.emarsys.net"
    }
}
