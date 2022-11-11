package com.emarsys.mobileengage.iam.webview

import android.os.Handler
import android.os.Looper
import androidx.test.rule.ActivityTestRule
import com.emarsys.core.provider.activity.CurrentActivityProvider
import com.emarsys.mobileengage.di.setupMobileEngageComponent
import com.emarsys.mobileengage.fake.FakeMobileEngageDependencyContainer
import com.emarsys.testUtil.fake.FakeActivity
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch

class WebViewProviderTest {
    lateinit var mockCurrentActivityProvider: CurrentActivityProvider
    lateinit var webViewProvider: WebViewProvider

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setUp() {
        mockCurrentActivityProvider = mock {
            on { get() }.doReturn(activityRule.activity)
        }

        val mockContainer =
            FakeMobileEngageDependencyContainer(currentActivityProvider = mockCurrentActivityProvider)

        setupMobileEngageComponent(mockContainer)

        webViewProvider = WebViewProvider()
    }

    @Test
    fun testProvideWebView() {
        val latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            val result = webViewProvider.provideEmarsysWebView()
            result.javaClass shouldBe EmarsysWebView::class.java
            latch.countDown()
        }
        latch.await()
    }
}