package com.emarsys.predict


import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.emarsys
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.di.tearDownEmarsysComponent


import com.emarsys.testUtil.AnnotationSpec
import org.mockito.Mockito
import org.mockito.kotlin.mock

class PredictRestrictedTest : AnnotationSpec() {
    private companion object {
        const val CONTACT_FIELD_ID = 999
    }

    private lateinit var mockPredictInternal: PredictInternal
    private lateinit var predictRestricted: PredictRestricted


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
        predictRestricted.setContact(CONTACT_FIELD_ID, "contactId", null)
        Mockito.verify(mockPredictInternal).setContact(CONTACT_FIELD_ID, "contactId", null)
    }

    @Test
    fun testPredict_clearContact_delegatesTo_Predict_Internal() {
        predictRestricted.clearContact()
        Mockito.verify(mockPredictInternal).clearContact()
    }
}