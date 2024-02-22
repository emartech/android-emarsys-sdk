package com.emarsys.mobileengage.iam.dialog

import androidx.test.core.app.ActivityScenario
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.testUtil.fake.FakeActivity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class IamDialogProviderTest {

    private lateinit var iamDialogProvider: IamDialogProvider
    private lateinit var scenario: ActivityScenario<FakeActivity>

    @BeforeEach
    fun setUp() {
        scenario = ActivityScenario.launch(FakeActivity::class.java)
        scenario.onActivity { activity ->
            iamDialogProvider = IamDialogProvider(
                ConcurrentHandlerHolderFactory.create(),
                mock(),
                mock(),
                mock(),
                mock()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testProvideDialog_shouldNotCrash_whenNotCalledFromUiThread() {
        val latch = CountDownLatch(1)
        thread(start = true) {
            iamDialogProvider.provideDialog(
                "campaignId", "id", "https://www.example.com", "reqId"
            )
            latch.countDown()
        }
        latch.await()
    }

    @Test
    fun testProvideDialog_shouldSetCorrectArguments() {
        val testSid = "test sid"
        val testCampaignId = "test campaign id"
        val testUrl = "test url"
        val testRequestId = "test request id"

        val resultDialog =
            iamDialogProvider.provideDialog(testCampaignId, testSid, testUrl, testRequestId)

        val resultArguments = resultDialog.arguments!!

        resultArguments.getString(IamDialog.CAMPAIGN_ID) shouldBe testCampaignId
        resultArguments.getString(IamDialog.SID) shouldBe testSid
        resultArguments.getString(IamDialog.URL) shouldBe testUrl
        resultArguments.getString(IamDialog.REQUEST_ID) shouldBe testRequestId
    }
}