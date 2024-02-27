package com.emarsys.core.provider.activity

import android.app.Activity
import com.emarsys.testUtil.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.ref.WeakReference

class CurrentActivityProviderTest : AnnotationSpec() {
    private lateinit var provider: CurrentActivityProvider
    private lateinit var mockFallbackActivityProvider: FallbackActivityProvider

    companion object {
        val activity1: Activity = mock()
        val activity2: Activity = mock()
    }


    @Before
    fun setUp() {
        mockFallbackActivityProvider = mock()
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

        whenever(mockFallbackActivityProvider.provide()).thenReturn(activity1)

        val result = provider.get()

        result shouldBe activity1
    }
}