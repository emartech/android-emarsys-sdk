package com.emarsys.mobileengage.iam.dialog

import android.os.Handler
import android.os.Looper
import androidx.test.rule.ActivityTestRule
import com.emarsys.testUtil.fake.FakeActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class IamDialogProviderTest {

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(FakeActivity::class.java, false)

    private lateinit var iamDialogProvider: IamDialogProvider

    @Before
    fun setUp() {
        iamDialogProvider = IamDialogProvider(Handler(Looper.getMainLooper()), mock())
    }

    @Test
    fun testProvideDialog_shouldNotCrash_whenNotCalledFromUiThread() {
        val latch = CountDownLatch(1)
        thread(start = true) {
            iamDialogProvider.provideDialog(
                    "campaignId", "id", "https://www.example.com", "reqId")
            latch.countDown()
        }
        latch.await()
    }
}