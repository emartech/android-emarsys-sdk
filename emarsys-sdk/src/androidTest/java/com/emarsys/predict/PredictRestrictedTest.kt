package com.emarsys.predict


import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.emarsys
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.di.tearDownEmarsysComponent
import com.emarsys.testUtil.TimeoutUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito
import org.mockito.kotlin.mock

class PredictRestrictedTest {
    private companion object {
        const val CONTACT_FIELD_ID = 999
    }

    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var predictRestricted: PredictRestricted

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockPredictInternal = mock()

        val dependencyContainer = FakeDependencyContainer(predictInternal = mockPredictInternal)

        setupEmarsysComponent(dependencyContainer)
        predictRestricted = PredictRestricted()
    }

    @After
    fun tearDown() {
        try {
            emarsys().concurrentHandlerHolder.coreLooper.quit()
            tearDownEmarsysComponent()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testPredict_setContact_delegatesTo_Predict_Internal() {
        predictRestricted.setContact(CONTACT_FIELD_ID, "contactId")
        Mockito.verify(mockPredictInternal).setContact(CONTACT_FIELD_ID, "contactId")
    }

    @Test
    fun testPredict_clearContact_delegatesTo_Predict_Internal() {
        predictRestricted.clearContact()
        Mockito.verify(mockPredictInternal).clearContact()
    }
}