package com.emarsys.predict

import com.emarsys.core.storage.KeyValueStore
import com.emarsys.test.util.TimeoutUtils
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class PredictInternalTest {

    @Rule
    @JvmField
    var timeout = TimeoutUtils.timeoutRule

    private lateinit var mockKeyValueStore: KeyValueStore
    private lateinit var predictInternal: PredictInternal

    @Before
    fun init() {
        mockKeyValueStore = mock(KeyValueStore::class.java)
        predictInternal = PredictInternal(mockKeyValueStore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_keyValueStore_shouldNotBeNull() {
        PredictInternal(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetCustomer_customerId_mustNotBeNull() {
        predictInternal.setCustomer(null)
    }

    @Test
    fun testSetCustomer_shouldPersistsWithKeyValueStore() {
        val customerId = "customerId"

        predictInternal.setCustomer(customerId)

        verify(mockKeyValueStore).putString("predict_customerId", customerId)
    }
}