package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.core.di.DependencyInjection
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.testUtil.IntegrationTestUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class MobileEngageTest {
    companion object {
        private const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        private const val CONTACT_ID_TOKEN = "idTOKENTOKENTOKENidTOKEN"
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
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testSetContact_delegatesToInternal() {
       mobileEngageApi.setContact(CONTACT_FIELD_VALUE, mockCompletionListener)
        verify(mockMobileEngageInternal).setContact(CONTACT_FIELD_VALUE, mockCompletionListener)
    }

    @Test
    fun testSetAuthorizedContact_delegatesToInternal() {
       mobileEngageApi.setAuthenticatedContact(CONTACT_ID_TOKEN, mockCompletionListener)
        verify(mockMobileEngageInternal).setAuthenticatedContact(CONTACT_ID_TOKEN, mockCompletionListener)
    }

    @Test
    fun testClearContact_delegatesToInternal() {
       mobileEngageApi.clearContact(mockCompletionListener)
        verify(mockMobileEngageInternal).clearContact(mockCompletionListener)
    }
}