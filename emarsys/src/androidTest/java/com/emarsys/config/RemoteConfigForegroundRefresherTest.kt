package com.emarsys.config

import androidx.lifecycle.LifecycleOwner
import com.emarsys.core.concurrency.ConcurrentHandlerHolderFactory
import com.emarsys.core.connection.ConnectionWatchDog
import com.emarsys.core.handler.ConcurrentHandlerHolder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class RemoteConfigForegroundRefresherTest {
    private lateinit var mockConfigInternal: DefaultConfigInternal
    private lateinit var mockConnectionWatchDog: ConnectionWatchDog
    private lateinit var concurrentHandlerHolder: ConcurrentHandlerHolder
    private lateinit var mockLifecycleOwner: LifecycleOwner
    private lateinit var refresher: RemoteConfigForegroundRefresher

    @Before
    fun setUp() {
        mockConfigInternal = mockk(relaxed = true)
        mockConnectionWatchDog = mockk(relaxed = true)
        mockLifecycleOwner = mockk(relaxed = true)
        concurrentHandlerHolder = ConcurrentHandlerHolderFactory.create()

        refresher = RemoteConfigForegroundRefresher(
            mockConfigInternal,
            mockConnectionWatchDog,
            concurrentHandlerHolder
        )
    }

    @Test
    fun onStart_shouldRefreshRemoteConfig_whenNotFetchedThisSessionAndConnected() {
        every { mockConfigInternal.hasFetchedThisSession } returns false
        every { mockConnectionWatchDog.isConnected } returns true

        refresher.onStart(mockLifecycleOwner)
        awaitCoreHandler()

        verify { mockConfigInternal.refreshRemoteConfig(null) }
    }

    @Test
    fun onStart_shouldNotRefreshRemoteConfig_whenAlreadyFetchedThisSession() {
        every { mockConfigInternal.hasFetchedThisSession } returns true
        every { mockConnectionWatchDog.isConnected } returns true

        refresher.onStart(mockLifecycleOwner)
        awaitCoreHandler()

        verify(exactly = 0) { mockConfigInternal.refreshRemoteConfig(any()) }
    }

    @Test
    fun onStart_shouldNotRefreshRemoteConfig_whenNotConnected() {
        every { mockConfigInternal.hasFetchedThisSession } returns false
        every { mockConnectionWatchDog.isConnected } returns false

        refresher.onStart(mockLifecycleOwner)
        awaitCoreHandler()

        verify(exactly = 0) { mockConfigInternal.refreshRemoteConfig(any()) }
    }

    private fun awaitCoreHandler() {
        val latch = CountDownLatch(1)
        concurrentHandlerHolder.coreHandler.post {
            latch.countDown()
        }
        latch.await()
    }
}
