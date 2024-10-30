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
import com.emarsys.core.storage.StringStorage
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.session.SessionIdHolder
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DefaultDeepLinkInternalTest : AnnotationSpec() {
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
        mockActivity = mock(Activity::class.java, Mockito.RETURNS_DEEP_STUBS)
        mockManager = mock(RequestManager::class.java)
        mockTimestampProvider = mock(TimestampProvider::class.java)
        mockUuidProvider = mock(UUIDProvider::class.java)
        whenever(mockUuidProvider.provideId()).thenReturn("REQUEST_ID")
        mockDeviceInfo = mock(DeviceInfo::class.java)
        whenever(mockDeviceInfo.sdkVersion).thenReturn("0.0.1")
        requestContext = MobileEngageRequestContext(
            APPLICATION_CODE,
            1,
            null,
            mockDeviceInfo,
            mockTimestampProvider,
            mockUuidProvider,
            mock(StringStorage::class.java),
            mock(StringStorage::class.java),
            mock(StringStorage::class.java),
            mock(StringStorage::class.java),
            mock(StringStorage::class.java),
            mock(SessionIdHolder::class.java)
        )
        mockDeepLinkServiceProvider = mock(ServiceEndpointProvider::class.java)
        Mockito.`when`(mockDeepLinkServiceProvider.provideEndpointHost()).thenReturn(DEEPLINK_BASE)
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
        argumentCaptor<RequestModel> {
            deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, null)
            Mockito.verify(mockManager)
                .submit(this.capture(), isNull())
            val result = this.firstValue
            assertRequestModels(expected, result)

        }
    }

    @Test
    fun testTrackDeepLink_requestManagerCalled_withCorrectCompletionHandler() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5")
        )
        val completionListener: CompletionListener = mock()
        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, completionListener)
        verify(mockManager).submit(
            anyOrNull(), eq(completionListener)
        )
    }

    @Test
    fun testTrackDeepLink_setsClickedFlag_onIntentBundle() {
        val originalIntent = mock(Intent::class.java)
        Mockito.`when`(mockActivity.intent).thenReturn(originalIntent)
        val currentIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5")
        )
        deepLinkInternal.trackDeepLinkOpen(mockActivity, currentIntent, null)
        verify(originalIntent).putExtra("ems_deep_link_tracked", true)
    }

    @Test
    fun testTrackDeepLink_doesNotCallRequestManager_whenTrackedFlagIsSet() {
        val intentFromActivity = mock(
            Intent::class.java
        )
        Mockito.`when`(mockActivity.intent).thenReturn(intentFromActivity)
        Mockito.`when`(intentFromActivity.getBooleanExtra("ems_deep_link_tracked", false))
            .thenReturn(true)
        val currentIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5")
        )
        deepLinkInternal.trackDeepLinkOpen(mockActivity, currentIntent, null)
        verify(mockManager, never()).submit(anyOrNull(), isNull())
    }

    @Test
    fun testTrackDeepLink_doesNotCallRequestManager_whenDataIsNull() {
        val intent = Intent()
        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, null)
        verify(mockManager, never()).submit(anyOrNull(), isNull())
    }

    @Test
    fun testTrackDeepLink_doesNotCallRequestManager_whenUriDoesNotContainEmsDlQueryParameter() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&other=1_2_3_4_5")
        )
        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent, null)
        verify(mockManager, never()).submit(anyOrNull(), isNull())
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
