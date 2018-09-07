package com.emarsys.predict

import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.RequestManager
import com.emarsys.core.shard.ShardModel
import com.emarsys.core.storage.KeyValueStore
import com.emarsys.testUtil.TimeoutUtils
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class PredictInternalTest {

    @Rule
    @JvmField
    var timeout = TimeoutUtils.timeoutRule

    private lateinit var mockKeyValueStore: KeyValueStore
    private lateinit var predictInternal: PredictInternal
    private lateinit var mockRequestManager: RequestManager
    private lateinit var mockTimestampProvider: TimestampProvider
    private lateinit var mockUuidProvider: UUIDProvider

    @Before
    fun init() {
        mockKeyValueStore = mock(KeyValueStore::class.java)
        mockRequestManager = mock(RequestManager::class.java)
        mockTimestampProvider = mock(TimestampProvider::class.java)
        mockUuidProvider = mock(UUIDProvider::class.java)

        predictInternal = PredictInternal(mockKeyValueStore, mockRequestManager, mockUuidProvider, mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_keyValueStore_shouldNotBeNull() {
        PredictInternal(null, mockRequestManager, mockUuidProvider, mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_repository_shouldNotBeNull() {
        PredictInternal(mockKeyValueStore, null, mockUuidProvider, mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_uuidProvider_shouldNotBeNull() {
        PredictInternal(mockKeyValueStore, mockRequestManager, null, mockTimestampProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_timestampProvider_shouldNotBeNull() {
        PredictInternal(mockKeyValueStore, mockRequestManager, mockUuidProvider, null)
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

    @Test(expected = IllegalArgumentException::class)
    fun testTrackItemView_itemId_mustNotBeNull() {
        predictInternal.trackItemView(null)
    }

    @Test
    fun testTrackItemView_shouldCallRepositoryWithCorrectShardModel() {
        `when`(mockTimestampProvider.provideTimestamp()).thenReturn(1L)
        `when`(mockUuidProvider.provideId()).thenReturn("id")

        val expectedShardModel =
                ShardModel(mockUuidProvider.provideId(),
                        "predict_item_view",
                        mapOf("view" to "itemId"),
                        mockTimestampProvider.provideTimestamp(),
                        Long.MAX_VALUE)

        predictInternal.trackItemView("itemId")

        verify(mockRequestManager).submit(expectedShardModel)
    }
}