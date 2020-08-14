package com.emarsys.mobileengage

import android.os.Handler
import android.os.Looper
import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.DependencyInjection
import com.emarsys.core.di.getDependency
import com.emarsys.di.FakeDependencyContainer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class MobileEngageTest {
    companion object {
        private val CONTACT_FIELD_VALUE = "testContactFieldValue"
    }
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mobileEngageApi: MobileEngage


    @Before
    fun setUp() {
        mockMobileEngageInternal = mock()
        mockCompletionListener = mock()
        mobileEngageApi = MobileEngage()

        DependencyInjection.setup(FakeDependencyContainer(mobileEngageInternal = mockMobileEngageInternal))
    }

    @After
    fun tearDown() {
        try {
            val looper: Looper? = getDependency<Handler>("coreSdkHandler").looper
            looper?.quitSafely()
            DependencyInjection.tearDown()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun testSetContact_delegatesToInternal() {
       mobileEngageApi.setContact(CONTACT_FIELD_VALUE, mockCompletionListener)
        verify(mockMobileEngageInternal).setContact(CONTACT_FIELD_VALUE, mockCompletionListener)
    }

    @Test
    fun testClearContact_delegatesToInternal() {
       mobileEngageApi.clearContact(mockCompletionListener)
        verify(mockMobileEngageInternal).clearContact(mockCompletionListener)
    }
}