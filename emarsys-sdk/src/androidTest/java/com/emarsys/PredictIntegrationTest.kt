package com.emarsys


import android.app.Application
import android.content.Context
import com.emarsys.config.EmarsysConfig
import com.emarsys.core.DefaultCoreCompletionHandler
import com.emarsys.core.api.result.Try
import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.LanguageProvider
import com.emarsys.core.notification.NotificationManagerHelper
import com.emarsys.core.provider.clientid.ClientIdProvider
import com.emarsys.core.provider.version.VersionProvider
import com.emarsys.core.response.ResponseModel
import com.emarsys.core.storage.Storage
import com.emarsys.di.DefaultEmarsysComponent
import com.emarsys.di.DefaultEmarsysDependencies
import com.emarsys.di.emarsys
import com.emarsys.predict.api.model.PredictCartItem
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationFilter
import com.emarsys.predict.api.model.RecommendationLogic
import com.emarsys.predict.util.CartItemUtils
import com.emarsys.testUtil.ConnectionTestUtils
import com.emarsys.testUtil.DatabaseTestUtils
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.InstrumentationRegistry
import com.emarsys.testUtil.IntegrationTestUtils
import com.emarsys.testUtil.rules.ConnectionRule
import com.emarsys.testUtil.rules.DuplicatedThreadRule
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KMutableProperty0


class PredictIntegrationTest  {
    @Rule
    @JvmField
    val duplicateThreadRule = DuplicatedThreadRule("CoreSDKHandlerThread")

    @Rule
    @JvmField
    val connectionRule = ConnectionRule(application)

    companion object {
        private const val CONTACT_FIELD_ID = 3
        private const val MERCHANT_ID = "1428C8EE286EC34B"
        private const val OTHER_MERCHANT_ID = "test_1428C8EE286EC34B"
        const val ITEM1 = "12800"
        const val ITEM2 = "13433"
        const val ITEM3 = "9129-P"
        const val SEARCH_TERM = "Ropa"
        const val CATEGORY_PATH = "Ropa bebe nina>Ropa Interior"
    }

    private lateinit var latch: CountDownLatch
    private lateinit var baseConfig: EmarsysConfig
    private lateinit var responseModel: ResponseModel
    private lateinit var completionHandler: DefaultCoreCompletionHandler
    private lateinit var responseModelMatches: (ResponseModel) -> Boolean
    private var errorCause: Throwable? = null
    private lateinit var clientStateStorage: Storage<String?>
    lateinit var triedRecommendedProducts: Try<List<Product>>

    private val application: Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as Application


    @Before
    fun setup() {
        DatabaseTestUtils.deleteCoreDatabase()

        application.getSharedPreferences("emarsys_secure_shared_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        baseConfig = EmarsysConfig.Builder()
            .application(application)
            .merchantId(MERCHANT_ID)
            .build()

        latch = CountDownLatch(1)
        errorCause = null

        ConnectionTestUtils.checkConnection(application)
        FeatureTestUtils.resetFeatures()
        responseModelMatches = {
            false
        }
        completionHandler = object : DefaultCoreCompletionHandler(mutableMapOf()) {
            override fun onSuccess(id: String, responseModel: ResponseModel) {
                super.onSuccess(id, responseModel)
                if (responseModel.isPredictRequest and this@PredictIntegrationTest.responseModelMatches(
                        responseModel
                    )
                ) {
                    this@PredictIntegrationTest.responseModel = responseModel
                    latch.countDown()
                }
            }

            override fun onError(id: String, cause: Exception) {
                super.onError(id, cause)
                this@PredictIntegrationTest.errorCause = cause
                latch.countDown()
            }

            override fun onError(id: String, responseModel: ResponseModel) {
                super.onError(id, responseModel)
                this@PredictIntegrationTest.responseModel = responseModel
                latch.countDown()
            }
        }


        val deviceInfo = DeviceInfo(
            application,
            mockk<ClientIdProvider>(relaxed = true).apply {
                every { provideClientId() } returns "mobileengage_integration_hwid"
            },
            mockk<VersionProvider>(relaxed = true).apply {
                every { provideSdkVersion() } returns "0.0.0-mobileengage_integration_version"
            },
            mockk<LanguageProvider>(relaxed = true).apply {
                every { provideLanguage(any()) } returns "en-US"
            },
            mockk<NotificationManagerHelper>(relaxed = true),
            isAutomaticPushSendingEnabled = true,
            isGooglePlayAvailable = true
        )

        DefaultEmarsysDependencies(baseConfig, object : DefaultEmarsysComponent(baseConfig) {
            override val coreCompletionHandler: DefaultCoreCompletionHandler
                get() = completionHandler
            override val deviceInfo: DeviceInfo
                get() = deviceInfo
        })

        emarsys().concurrentHandlerHolder.coreHandler.post {
            emarsys().clientStateStorage.remove()
            emarsys().contactFieldValueStorage.remove()
            emarsys().contactTokenStorage.remove()
            emarsys().pushTokenStorage.remove()
        }

        Emarsys.setup(baseConfig)

        emarsys().concurrentHandlerHolder.coreHandler.post {
            emarsys().clientServiceStorage.remove()
            emarsys().eventServiceStorage.remove()
            emarsys().deepLinkServiceStorage.remove()
            emarsys().messageInboxServiceStorage.remove()
            emarsys().predictServiceStorage.remove()
        }
    }

    @After
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys(application)
    }

    @Test
    fun testTrackCart() {
        val cartItems = listOf(
            PredictCartItem(ITEM1, 1.1, 10.0),
            PredictCartItem(ITEM2, 2.2, 20.0),
            PredictCartItem(ITEM3, 3.3, 30.0)
        )

        responseModelMatches = {
            it.baseUrl.contains(CartItemUtils.cartItemsToQueryParam(cartItems))
        }

        Emarsys.predict.trackCart(cartItems)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackPurchase() {
        val cartItems = listOf(
            PredictCartItem(ITEM1, 1.1, 10.0),
            PredictCartItem(ITEM2, 2.2, 20.0),
            PredictCartItem(ITEM3, 3.3, 30.0)
        )

        val orderId = "orderId_1234567892345678"

        responseModelMatches = {
            it.baseUrl.contains(CartItemUtils.cartItemsToQueryParam(cartItems))
            it.baseUrl.contains(orderId)
        }

        Emarsys.predict.trackPurchase(orderId, cartItems)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackItemView() {
        val itemId = ITEM3
        responseModelMatches = {
            it.baseUrl.contains(itemId)
        }

        Emarsys.predict.trackItemView(itemId)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackItemView_withUrlEncodableCharacter() {
        val itemId = "2508+"
        responseModelMatches = {
            it.requestModel.url.toString().contains("v=i%3A2508%252B")
        }

        Emarsys.predict.trackItemView(itemId)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackItemView_withProduct() {
        val product = Product(ITEM3, "TestTitle", "https://emarsys.com", "RELATED", "AAAA")
        responseModelMatches = {
            it.baseUrl.contains(product.productId)
        }

        Emarsys.predict.trackRecommendationClick(product)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackCategoryView() {
        responseModelMatches = {
            it.baseUrl.contains(CATEGORY_PATH)
        }

        Emarsys.predict.trackCategoryView(CATEGORY_PATH)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackSearchTerm() {
        responseModelMatches = {
            it.baseUrl.contains(SEARCH_TERM)
        }

        Emarsys.predict.trackSearchTerm(SEARCH_TERM)

        eventuallyAssertSuccess()
    }

    @Test
    fun testTrackTag() {
        val tag = "testTag"
        responseModelMatches = {
            it.baseUrl.contains(tag)
        }

        Emarsys.predict.trackTag(tag, mapOf("testKey" to "testValue"))

        eventuallyAssertSuccess()
    }

    @Test
    fun testRecommendProducts() {
        Emarsys.predict.recommendProducts(
            RecommendationLogic.search(SEARCH_TERM),
            listOf(RecommendationFilter.exclude("price").isValue("")),
            3,
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        ).eventuallyAssert {
            latch.await()

            triedRecommendedProducts.errorCause shouldBe null
            triedRecommendedProducts.result shouldNotBe null
        }
    }

    @Test
    fun testRecommendProducts_withSearch() {
        Emarsys.predict.recommendProducts(
            RecommendationLogic.search(SEARCH_TERM),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPreviousSearch() {
        testTrackSearchTerm()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.search(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withCart() {
        val cartItems = listOf(
            PredictCartItem(ITEM1, 1.1, 10.0),
            PredictCartItem(ITEM2, 2.2, 20.0),
            PredictCartItem(ITEM3, 3.3, 30.0)
        )
        Emarsys.predict.recommendProducts(
            RecommendationLogic.cart(cartItems),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPreviousCart() {
        testTrackCart()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.cart(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withRelated() {
        Emarsys.predict.recommendProducts(
            RecommendationLogic.related(ITEM3),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_related_withPreviousView() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.related(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withoutRelated() {
        Emarsys.predict.recommendProducts(
            RecommendationLogic.related(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        ).eventuallyAssert {
            triedRecommendedProducts.errorCause shouldBe null
            triedRecommendedProducts.result shouldNotBe null
            triedRecommendedProducts.result!!.size shouldBe 0
        }
    }

    @Test
    fun testRecommendProducts_withCategory() {
        Emarsys.predict.recommendProducts(
            RecommendationLogic.category(CATEGORY_PATH),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPreviousCategory() {
        testTrackCategoryView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.category(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withAlsoBought() {
        Emarsys.predict.recommendProducts(
            RecommendationLogic.alsoBought(ITEM1),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_alsoBought_withPreviousViewItem() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.alsoBought(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_popular_withPreviousCategory() {
        testTrackCategoryView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.popular(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPersonal() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.personal(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withPersonalVariants() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.personal(listOf("1", "2", "3")),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withHome() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.home(),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withHomeVariants() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.home(listOf("1", "2", "3")),
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

        eventuallyAssertForTriedRecommendedProducts()
    }

    @Test
    fun testRecommendProducts_withAvailabilityZone() {
        testTrackItemView()
        latch = CountDownLatch(1)

        Emarsys.predict.recommendProducts(
            RecommendationLogic.home(listOf("1", "2", "3")), "hu",
            eventuallyStoreResultInProperty(this::triedRecommendedProducts.setter)
        )

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
        emarsys().concurrentHandlerHolder.coreHandler.post {
            clientStateStorage = emarsys().clientStateStorage
            clientStateStorage.set("predict-integration-test")
        }


        Emarsys.setContact(CONTACT_FIELD_ID, "test@test.com")
        testMultipleInvocations()
    }

    @Test
    fun testConfig_changeMerchantId() {
        val originalMerchantId = Emarsys.config.merchantId
        Emarsys.config.changeMerchantId(OTHER_MERCHANT_ID)
        originalMerchantId shouldNotBe Emarsys.config.applicationCode
        Emarsys.config.merchantId shouldBe OTHER_MERCHANT_ID
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
        get() = this.requestModel.url.toString()
            .startsWith("https://recommender.scarabresearch.com/merchants/$MERCHANT_ID?")

    private val ResponseModel.baseUrl
        get() = URLDecoder.decode(this.requestModel.url.toString(), "UTF-8")
}