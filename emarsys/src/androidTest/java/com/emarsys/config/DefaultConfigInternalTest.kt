package com.emarsys.config

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.feature.FeatureRegistry
import com.emarsys.core.storage.Storage
import com.emarsys.feature.InnerFeature
import com.emarsys.mobileengage.MobileEngageInternal
import com.emarsys.mobileengage.MobileEngageRequestContext
import com.emarsys.mobileengage.push.PushInternal
import com.emarsys.mobileengage.push.PushTokenProvider
import com.emarsys.predict.PredictInternal
import com.emarsys.predict.request.PredictRequestContext
import com.emarsys.testUtil.FeatureTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.whenever
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch

class DefaultConfigInternalTest {
    private companion object {
        const val APPLICATION_CODE = "applicationCode"
        const val MERCHANT_ID = "merchantId"
        const val OTHER_APPLICATION_CODE = "otherApplicationCode"
        const val CONTACT_FIELD_ID = 3
        const val CONTACT_FIELD_VALUE = "originalContactFieldValue"
        const val PUSH_TOKEN = "pushToken"
    }

    private lateinit var configInternal: ConfigInternal
    private lateinit var mockMobileEngageRequestContext: MobileEngageRequestContext
    private lateinit var mockPredictRequestContext: PredictRequestContext
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockPushInternal: PushInternal
    private lateinit var mockPushTokenProvider: PushTokenProvider
    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var mockContactFieldValueStorage: Storage<String>
    private lateinit var latch: CountDownLatch

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setUp() {
        FeatureTestUtils.resetFeatures()

        latch = CountDownLatch(1)

        mockPushTokenProvider = mock(PushTokenProvider::class.java).apply {
            whenever(providePushToken()).thenReturn(PUSH_TOKEN)
        }

        mockContactFieldValueStorage = (mock(Storage::class.java) as Storage<String>).apply {
            whenever(get()).thenReturn(CONTACT_FIELD_VALUE).thenReturn(null)
        }

        mockPredictRequestContext = mock(PredictRequestContext::class.java).apply {
            whenever(merchantId).thenReturn(MERCHANT_ID)
        }

        mockMobileEngageRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
            whenever(contactFieldId).thenReturn(CONTACT_FIELD_ID)
        }
        mockMobileEngageInternal = mock(MobileEngageInternal::class.java).apply {
            whenever(clearContact(any())).thenAnswer { invocation ->
                mockContactFieldValueStorage.get()
                (invocation.getArgument(0) as CompletionListener?)?.onCompleted(null)
            }
            whenever(setContact(any(), any())).thenAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener?)?.onCompleted(null)
            }
        }
        mockPushInternal = mock(PushInternal::class.java).apply {
            whenever(setPushToken(any(), any())).thenAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener?)?.onCompleted(null)
            }
        }

        mockPredictInternal = mock(PredictInternal::class.java)

        configInternal = DefaultConfigInternal(mockMobileEngageRequestContext, mockMobileEngageInternal, mockPushInternal, mockPushTokenProvider, mockPredictRequestContext)
    }

    @After
    fun tearDown() {
        FeatureTestUtils.resetFeatures()
    }

    @Test
    fun testGetContactFieldId_shouldReturnValueFromRequestContext() {
        val result = configInternal.contactFieldId

        result shouldBe CONTACT_FIELD_ID
    }

    @Test
    fun testGetApplicationCode_shouldReturnValueFromRequestContext() {
        val result = configInternal.applicationCode

        result shouldBe APPLICATION_CODE
    }

    @Test
    fun testGetMerchantId_shouldReturnValueFromRequestContext() {
        val result = configInternal.merchantId

        result shouldBe MERCHANT_ID
    }

    @Test
    fun testChangeApplicationCode_shouldCallClearContact() {
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener { })

        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
    }

    @Test
    fun testChangeApplicationCode_shouldCallSetContactWithOriginalContactFieldIdAndContactFieldValue() {
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
            verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())
        })
    }

    @Test
    fun testChangeApplicationCode_shouldCallSetPushToken() {
        val latch = CountDownLatch(1)
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })
        latch.await()

        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
    }

    @Test
    fun testChangeApplicationCode_shouldChangeApplicationCodeAfterClearContact() {
        val latch = CountDownLatch(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })
        latch.await()
        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        inOrder.verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringClearContact() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)
        val mockMobileEngageInternal = mock(MobileEngageInternal::class.java)
        whenever(mockMobileEngageInternal.clearContact(any())).thenAnswer { invocation ->
            (invocation.getArgument(0) as CompletionListener).onCompleted(Throwable())
        }
        configInternal = DefaultConfigInternal(mockMobileEngageRequestContext, mockMobileEngageInternal, mockPushInternal, mockPushTokenProvider, mockPredictRequestContext)
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)
        latch.await()
        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockMobileEngageRequestContext).applicationCode
        verify(mockMobileEngageRequestContext).contactFieldValueStorage
        verifyZeroInteractions(mockPushInternal)
        verifyNoMoreInteractions(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
        verifyNoMoreInteractions(mockMobileEngageRequestContext)

    }

    @Test
    fun testChangeApplicationCode_shouldDoOnlyLogin_whenApplicationCode_isNull() {
        val latch = CountDownLatch(1)

        val mockMobileEngageRequestContext = mock(MobileEngageRequestContext::class.java).apply {
            whenever(applicationCode).thenReturn(null)
            whenever(contactFieldValueStorage).thenReturn(mockContactFieldValueStorage)
        }

        configInternal = DefaultConfigInternal(mockMobileEngageRequestContext, mockMobileEngageInternal, mockPushInternal, mockPushTokenProvider, mockPredictRequestContext)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })

        latch.await()

        verify(mockMobileEngageRequestContext).contactFieldValueStorage
        verify(mockMobileEngageRequestContext).applicationCode
        verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())

        verify(mockMobileEngageInternal, times(0)).clearContact(any(CompletionListener::class.java))
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringSetPushToken() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val mockPushInternal = mock(PushInternal::class.java).apply {
            whenever(setPushToken(any(), any())).thenAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener).onCompleted(Throwable())
            }
        }
        configInternal = DefaultConfigInternal(mockMobileEngageRequestContext, mockMobileEngageInternal, mockPushInternal, mockPushTokenProvider, mockPredictRequestContext)

        val completionListener = CompletionListener {
            latch.countDown()
        }

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)
        latch.await()

        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        verifyNoMoreInteractions(mockMobileEngageInternal)
        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
    }

    @Test
    fun testChangeApplicationCode_shouldInterruptFlow_andDisableFeature_whenErrorHappenedDuringSetContact() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        val mockMobileEngageInternal = mock(MobileEngageInternal::class.java).apply {
            whenever(setContact(any(), any())).thenAnswer { invocation ->
                (invocation.getArgument(1) as CompletionListener).onCompleted(Throwable())
            }
            whenever(clearContact(any())).thenAnswer { invocation ->
                (invocation.getArgument(0) as CompletionListener).onCompleted(null)
            }
        }
        configInternal = DefaultConfigInternal(mockMobileEngageRequestContext, mockMobileEngageInternal, mockPushInternal, mockPushTokenProvider, mockPredictRequestContext)

        val completionListener = CompletionListener {
            latch.countDown()
        }

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, completionListener)

        latch.await()

        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockPushInternal).setPushToken(eq(PUSH_TOKEN), any())
        verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any(CompletionListener::class.java))

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
        verify(mockMobileEngageRequestContext).applicationCode = null
    }

    @Test
    fun testChangeApplicationCode_shouldWorkWithoutCompletionListener() {
        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, null)

        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(mockMobileEngageInternal, timeout(50)).clearContact(any(CompletionListener::class.java))
        inOrder.verify(mockMobileEngageRequestContext, timeout(50)).applicationCode = OTHER_APPLICATION_CODE
        inOrder.verify(mockPushInternal, timeout(50)).setPushToken(eq(PUSH_TOKEN), any())
        inOrder.verify(mockMobileEngageInternal, timeout(50)).setContact(eq(CONTACT_FIELD_VALUE), any())
    }

    @Test
    fun testChangeApplicationCode_shouldEnableFeature() {
        configInternal.changeApplicationCode(APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })

        latch.await()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe true
    }

    @Test
    fun testChangeApplicationCode_shouldDisableFeature() {
        FeatureRegistry.enableFeature(InnerFeature.MOBILE_ENGAGE)

        configInternal.changeApplicationCode(null, CompletionListener {
            latch.countDown()
        })

        latch.await()

        FeatureRegistry.isFeatureEnabled(InnerFeature.MOBILE_ENGAGE) shouldBe false
    }

    @Test
    fun testChangeApplicationCode_shouldOnlyLogout_whenApplicationCodeIsNull() {
        val latch = CountDownLatch(1)
        val completionListener = CompletionListener {
            latch.countDown()
        }
        configInternal.changeApplicationCode(null, completionListener)
        latch.await()
        verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        verify(mockMobileEngageRequestContext).contactFieldValueStorage
        verify(mockMobileEngageRequestContext).applicationCode
        verifyNoMoreInteractions(mockMobileEngageRequestContext)
        verifyZeroInteractions(mockPushInternal)
        verifyNoMoreInteractions(mockMobileEngageInternal)
    }

    @Test
    fun testChangeApplicationCode_shouldNotSendPushToken_whenPushTokenIsNull() {
        whenever(mockPushTokenProvider.providePushToken()).thenReturn(null)
        val latch = CountDownLatch(1)

        configInternal.changeApplicationCode(OTHER_APPLICATION_CODE, CompletionListener {
            latch.countDown()
        })
        latch.await()
        val inOrder = inOrder(mockMobileEngageInternal, mockPushInternal, mockMobileEngageRequestContext)
        inOrder.verify(mockMobileEngageInternal).clearContact(any(CompletionListener::class.java))
        inOrder.verify(mockMobileEngageRequestContext).applicationCode = OTHER_APPLICATION_CODE
        verifyZeroInteractions(mockPushInternal)
        inOrder.verify(mockMobileEngageInternal).setContact(eq(CONTACT_FIELD_VALUE), any())

    }

    @Test
    fun testChangeMerchantId_shouldEnableFeature() {
        configInternal.changeMerchantId(MERCHANT_ID)

        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe true
    }

    @Test
    fun testChangeMerchantId_shouldDisableFeature() {
        FeatureRegistry.enableFeature(InnerFeature.PREDICT)

        configInternal.changeMerchantId(null)

        FeatureRegistry.isFeatureEnabled(InnerFeature.PREDICT) shouldBe false
    }

    @Test
    fun testChangeMerchantId_shouldSaveMerchantId() {
        configInternal.changeMerchantId(MERCHANT_ID)

        verify(mockPredictRequestContext).merchantId = MERCHANT_ID
    }
}