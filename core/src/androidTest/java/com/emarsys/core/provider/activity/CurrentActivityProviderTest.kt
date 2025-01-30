package com.emarsys.core.provider.activity

import android.app.Activity
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.lang.ref.WeakReference

class CurrentActivityProviderTest  {
    private lateinit var provider: CurrentActivityProvider
    private lateinit var mockFallbackActivityProvider: FallbackActivityProvider

    companion object {
        val activity1: Activity = mockk(relaxed = true)
        val activity2: Activity = mockk(relaxed = true)
    }

    @Before
    fun setUp() {
        mockFallbackActivityProvider = mockk(relaxed = true)
        provider = CurrentActivityProvider(WeakReference(null), mockFallbackActivityProvider)
    }

    @Test
    fun testGet_returnsSetValue() {
        provider.set(activity1)
        provider.get() shouldBe activity1
    }

    @Test
    fun testSet_overridesPreviousValue() {
        provider.set(activity1)
        provider.set(activity2)
        provider.get() shouldBe activity2
    }

    @Test
    fun testGet_getsActivityWithReflection_whenValueIsNotSet() {
        provider.set(null)

        every { mockFallbackActivityProvider.provide() } returns activity1

        val result = provider.get()

        result shouldBe activity1
    }
}