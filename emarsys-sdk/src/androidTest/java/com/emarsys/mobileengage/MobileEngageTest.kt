package com.emarsys.mobileengage

import com.emarsys.core.api.result.CompletionListener
import com.emarsys.di.FakeDependencyContainer
import com.emarsys.di.setupEmarsysComponent
import com.emarsys.testUtil.IntegrationTestUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MobileEngageTest {
    companion object {
        private const val CONTACT_FIELD_ID = 999
        private const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        private const val CONTACT_ID_TOKEN = "idTOKENTOKENTOKENidTOKEN"
    }
    private lateinit var mockMobileEngageInternal: MobileEngageInternal
    private lateinit var mockCompletionListener: CompletionListener
    private lateinit var mobileEngageApi: MobileEngage


    @BeforeEach
    fun setUp() {
        mockMobileEngageInternal = mock()
        mockCompletionListener = mock()
        mobileEngageApi = MobileEngage()

        setupEmarsysComponent(FakeDependencyContainer(mobileEngageInternal = mockMobileEngageInternal))
    }

    @AfterEach
    fun tearDown() {
        IntegrationTestUtils.tearDownEmarsys()
    }

    @Test
    fun testSetContact_delegatesToInternal() {
       mobileEngageApi.setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, mockCompletionListener)
        verify(mockMobileEngageInternal).setContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE, mockCompletionListener)
    }

    @Test
    fun testSetAuthorizedContact_delegatesToInternal() {
       mobileEngageApi.setAuthenticatedContact(CONTACT_FIELD_ID, CONTACT_ID_TOKEN, mockCompletionListener)
        verify(mockMobileEngageInternal).setAuthenticatedContact(
            CONTACT_FIELD_ID,
            CONTACT_ID_TOKEN,
            mockCompletionListener
        )
    }

    @Test
    fun testClearContact_delegatesToInternal() {
       mobileEngageApi.clearContact(mockCompletionListener)
        verify(mockMobileEngageInternal).clearContact(mockCompletionListener)
    }
}