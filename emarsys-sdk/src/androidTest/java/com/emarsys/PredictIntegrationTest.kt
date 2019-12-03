package com.emarsys

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.rule.ActivityTestRule
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.api.result.Try
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.notification.NotificationManagerProxy
import com.emarsys.core.provider.hardwareid.HardwareIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.di.DefaultEmarsysDependencyContainer
import com.emarsys.di.EmarsysDependencyContainer
import com.emarsys.predict.api.model.PredictCartItem
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.api.model.RecommendationLogic
import com.emarsys.predict.util.CartItemUtils
import com.emarsys.testUtil.*
import com.emarsys.testUtil.fake.FakeActivity
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KMutableProperty0

class PredictIntegrationTest {

    companion object {
        private const val CONTACT_FIELD_ID = 3
        private const val MERCHANT_ID = "1428C8EE286EC34B"
        private const val OTHER_MERCHANT_ID = "test_1428C8EE286EC34B"
    }

    private lateinit var latch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var responseModel: ResponseModel
    private lateinit var completionHandler: DefaultCoreCompletionHandler
    private lateinit var responseModelMatches: (ResponseModel) -> Boolean
    private var errorCause: Throwable? = null
    private lateinit var clientStateStorage: Storage<String>
    private lateinit var contactTokenStorage: Storage<String>
    private lateinit var refreshTokenStorage: Storage<String>
    private lateinit var deviceInfoHashStorage: Storage<Int>
    lateinit var triedRecommendedProducts: Try<List<Product>>

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(FakeActivity::class.java)

    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        application.getSharedPreferences("emarsys_shared_preferences", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()

        baseConfig = EmarsysConfig.Builder()
                .application(application)
                .contactFieldId(CONTACT_FIELD_ID)
                .predictMerchantId(MERCHANT_ID)
                .build()

        latch = CountDownLatch(1)
        errorCause = null

        ConnectionTestUtils.checkConnection(application)
        FeatureTestUtils.resetFeatures()
        responseModelMatches = {
            false
        }
        completionHandler = object : DefaultCoreCompletionHandler(mutableMapOf()) {
            override fun onSuccess(id: String?, responseModel: ResponseModel) {
                super.onSuccess(id, responseModel)
                if (responseModel.isPredictRequest and this@PredictIntegrationTest.responseModelMatches(responseModel)) {
                    this@PredictIntegrationTest.responseModel = responseModel
                    latch.countDown()
                }
            }

            override fun onError(id: String?, cause: Exception) {
                super.onError(id, cause)
                this@PredictIntegrationTest.errorCause = cause
                latch.countDown()
            }

            override fun onError(id: String?, responseModel: ResponseModel) {
                super.onError(id, responseModel)
                this@PredictIntegrationTest.responseModel = responseModel
                latch.countDown()
            }
        }
        DependencyInjection.setup(object : DefaultEmarsysDependencyContainer(baseConfig) {
            override fun getCoreCompletionHandler() = completionHandler

            override fun getDeviceInfo() = DeviceInfo(
                    application,
                    mock(HardwareIdProvider::class.java).apply {
                        whenever(provideHardwareId()).thenReturn("predict_integration_hwid")
                    },
                    mock(VersionProvider::class.java).apply {
                        whenever(provideSdkVersion()).thenReturn("0.0.0-predict_integration_version")
                    },
                    mock(LanguageProvider::class.java).apply {
                        whenever(provideLanguage(ArgumentMatchers.any())).thenReturn("en-US")
                    },
                    NotificationManagerHelper(NotificationManagerProxy(application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager, NotificationManagerCompat.from(application))),
                    true
            )
        })

        clientStateStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.clientStateStorage
        contactTokenStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.contactTokenStorage
        refreshTokenStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().requestContext.refreshTokenStorage
        deviceInfoHashStorage = DependencyInjection.getContainer<DefaultEmarsysDependencyContainer>().deviceInfoHashStorage
        clientStateStorage.remove()
        contactTokenStorage.remove()
        refreshTokenStorage.remove()
        deviceInfoHashStorage.remove()

        Emarsys.setup(baseConfig)

        DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.set(null)
        DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.set(null)
    }

    @After
    fun tearDown() {
        try {
            with(DependencyInjection.getContainer<EmarsysDependencyContainer>()) {
                application.unregisterActivityLifecycleCallbacks(activityLifecycleWatchdog)
                application.unregisterActivityLifecycleCallbacks(currentActivityWatchdog)
                coreSdkHandler.looper.quitSafely()
            }

            clientStateStorage.remove()
            contactTokenStorage.remove()
            refreshTokenStorage.remove()
            deviceInfoHashStorage.remove()
            DependencyInjection.getContainer<EmarsysDependencyContainer>().clientServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().eventServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().deepLinkServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().mobileEngageV2ServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().inboxServiceStorage.set(null)
            DependencyInjection.getContainer<EmarsysDependencyContainer>().predictServiceStorage.set(null)
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testTrackCart() {
        val cartItems = listOf(
                PredictCartItem("2168", 1.1, 10.0),
                PredictCartItem("2200", 2.2, 20.0),
                PredictCartItem("2509", 3.3, 30.0)
        )

        responseModelMatches = {
            it.baseUrl.contains(CartItemUtils.cartItemsToQueryParam(cartItems))
        }

        Emarsys.Predict.trackCart(cartItems)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackPurchase() {
        val cartItems = listOf(
                PredictCartItem("2168", 1.1, 10.0),
                PredictCartItem("2200", 2.2, 20.0),
                PredictCartItem("2509", 3.3, 30.0)
        )

        val orderId = "orderId_1234567892345678"

        responseModelMatches = {
            it.baseUrl.contains(CartItemUtils.cartItemsToQueryParam(cartItems))
            it.baseUrl.contains(orderId)
        }

        Emarsys.Predict.trackPurchase(orderId, cartItems)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackItemView() {
        val itemId = "2168"
        responseModelMatches = {
            it.baseUrl.contains(itemId)
        }

        Emarsys.Predict.trackItemView(itemId)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackItemView_withProduct() {
        val product = Product.Builder("2168", "TestTitle", "https://emarsys.com", "RELATED", "AAAA").build()
        responseModelMatches = {
            it.baseUrl.contains(product.productId)
        }

        Emarsys.Predict.trackRecommendationClick(product)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackCategoryView() {
        val categoryId = "MEN>Shirts"
        responseModelMatches = {
            it.baseUrl.contains(categoryId)
        }

        Emarsys.Predict.trackCategoryView(categoryId)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackSearchTerm() {
        val searchTerm = "polo shirt"
        responseModelMatches = {
            it.baseUrl.contains(searchTerm)
        }

        Emarsys.Predict.trackSearchTerm(searchTerm)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackTag() {
        val tag = "testTag"
        responseModelMatches = {
            it.baseUrl.contains(tag)
        }

        Emarsys.Predict.trackTag(tag, mapOf("testKey" to "testValue"))

        eventuallyAssertSuccess()
    }

    @Test
    fun testRecommendProducts() {
        Emarsys.Predict.recommendProducts(RecommendationLogic.search("polo shirt"),
                listOf(RecommendationFilter.exclude("price").isValue("")),
                3,
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)).eventuallyAssert {
            latch.await()

            triedRecommendedProducts.errorCause shouldBe null
            triedRecommendedProducts.result shouldNotBe null
            triedRecommendedProducts.result!!.size shouldBe 3
        }
    }

    @Test
    fun testRecommendProducts_withSearch() {
        Emarsys.Predict.recommendProducts(RecommendationLogic.search("polo shirt"),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPreviousSearch() {
        testTrackSearchTerm()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.search(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withCart() {
        val cartItems = listOf(
                PredictCartItem("2168", 1.1, 10.0),
                PredictCartItem("2200", 2.2, 20.0),
                PredictCartItem("2509", 3.3, 30.0)
        )
        Emarsys.Predict.recommendProducts(RecommendationLogic.cart(cartItems),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPreviousCart() {
        testTrackCart()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.cart(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withRelated() {
        Emarsys.Predict.recommendProducts(RecommendationLogic.related("2200"),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_related_withPreviousView() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.related(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withoutRelated() {
        Emarsys.Predict.recommendProducts(RecommendationLogic.related(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)).eventuallyAssert {
            triedRecommendedProducts.errorCause shouldBe null
            triedRecommendedProducts.result shouldNotBe null
            triedRecommendedProducts.result!!.size shouldBe 0
        }
    }

    @Test
    fun testRecommendProducts_withCategory() {
        Emarsys.Predict.recommendProducts(RecommendationLogic.category("MEN>Shirts"),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPreviousCategory() {
        testTrackCategoryView()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.category(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withAlsoBought() {
        Emarsys.Predict.recommendProducts(RecommendationLogic.alsoBought("2200"),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_alsoBought_withPreviousViewItem() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.alsoBought(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_popular_withPreviousCategory() {
        testTrackCategoryView()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.popular(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPersonal() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.personal(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPersonalVariants() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.personal(listOf("1", "2", "3")),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withHome() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.home(),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withHomeVariants() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.Predict.recommendProducts(RecommendationLogic.home(listOf("1", "2", "3")),
                eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter))

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testMultipleInvocations() {
        testTrackCart()
        latch = CountDownLatch(1)
        testTrackPurchase()
        latch = CountDownLatch(1)
        testTrackCategoryView()
        latch = CountDownLatch(1)
        testTrackItemView()
        latch = CountDownLatch(1)
        testTrackSearchTerm()
        latch = CountDownLatch(1)
    }

    @Test
    fun testMultipleInvocationsWithSetContact() {
        clientStateStorage.set("predict-integration-test")

        Emarsys.setContact("test@test.com")
        testMultipleInvocations()
    }

    @Test
    fun testConfig_changeMerchantId() {
        val originalMerchantId = Emarsys.Config.merchantId
        Emarsys.Config.changeMerchantId(OTHER_MERCHANT_ID)
        originalMerchantId shouldNotBe Emarsys.Config.applicationCode
        Emarsys.Config.merchantId shouldBe OTHER_MERCHANT_ID
    }

    private fun eventuallyAssertSuccess() {
        latch.await()
        errorCause shouldBe null
        responseModel.statusCode shouldBe 200
    }

    private fun eventuallyAssertForTriedRecommendedProducts() {
        latch.await()

        triedRecommendedProducts.errorCause shouldBe null
        triedRecommendedProducts.result shouldNotBe null
        triedRecommendedProducts.result!!.size shouldBeGreaterThan 0
    }

    private fun <T> eventuallyStoreResultInProperty(setter: KMutableProperty0.Setter<T>): (T) -> Unit {
        return {
            setter(it)
            latch.countDown()
        }
    }

    private fun Any.eventuallyAssert(assertion: () -> Unit) {
        latch.await()
        assertion()
    }

    private val ResponseModel.isPredictRequest
        get() = this.requestModel.url.toString().startsWith("https://recommender.scarabresearch.com/merchants/$MERCHANT_ID?")

    private val ResponseModel.baseUrl
        get() = URLDecoder.decode(this.requestModel.url.toString(), "UTF-8")
}