package com.emarsys.core.di

import com.emarsys.testUtil.ReflectionTestUtils
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.matchers.beTheSameInstanceAs
import io.kotlintest.should
import io.kotlintest.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class DependencyInjectionTest {

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun init() {
        ReflectionTestUtils.setStaticField(DependencyInjection::class.java, "container", null)
    }

    @After
    fun tearDown() {
        ReflectionTestUtils.setStaticField(DependencyInjection::class.java, "container", null)
    }

    @Test
    fun testSetup_initializesContainer() {
        val mock = mock(DependencyContainer::class.java)
        DependencyInjection.setup(mock)

        val result = DependencyInjection.getContainer<DependencyContainer>()

        result should beTheSameInstanceAs(mock)
    }

    @Test
    fun testSetup_ignoresMultipleSetups() {
        val mock = mock(DependencyContainer::class.java)
        val mock2 = mock(DependencyContainer::class.java)
        DependencyInjection.setup(mock)
        DependencyInjection.setup(mock2)

        val result = DependencyInjection.getContainer<DependencyContainer>()

        result should beTheSameInstanceAs(mock)
    }

    @Test(expected = IllegalStateException::class)
    fun testGetContainer_shouldThrowException_whenContainerIsNotInitialized() {
        DependencyInjection.getContainer<DependencyContainer>()
    }

    @Test(expected = IllegalStateException::class)
    fun testGetContainer_shouldThrowException_afterTearDown() {
        DependencyInjection.setup(mock(DependencyContainer::class.java))
        DependencyInjection.tearDown()

        DependencyInjection.getContainer<DependencyContainer>()
    }

    @Test
    fun testIsSetUp_returnsTrue_ifInitialized() {
        DependencyInjection.setup(mock(DependencyContainer::class.java))

        DependencyInjection.isSetup() shouldBe true
    }

    @Test
    fun testIsSetUp_returnsFalse_withoutInitialization() {
        DependencyInjection.isSetup() shouldBe false
    }

    @Test
    fun testIsSetUp_returnsFalse_afterTearDown() {
        DependencyInjection.setup(mock(DependencyContainer::class.java))
        DependencyInjection.tearDown()

        DependencyInjection.isSetup() shouldBe false
    }

}