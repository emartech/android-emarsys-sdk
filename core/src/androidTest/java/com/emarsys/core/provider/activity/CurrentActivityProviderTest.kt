package com.emarsys.core.provider.activity

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.emarsys.testUtil.TimeoutUtils.timeoutRule
import com.emarsys.testUtil.fake.FakeActivity
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock

class CurrentActivityProviderTest {
    init {
        mock<Activity>()
    }

    private lateinit var provider: CurrentActivityProvider

    @Rule
    @JvmField
    var timeout: TestRule = timeoutRule

    @Rule
    @JvmField
    var activityScenarioRule: ActivityScenarioRule<FakeActivity> = ActivityScenarioRule(FakeActivity::class.java)

    @Before
    fun init() {
        provider = CurrentActivityProvider()
    }

    @Test
    fun testGet_returnsSetValue() {
        val activity: Activity = mock()
        provider.set(activity)
        provider.get() shouldBe activity
    }

    @Test
    fun testSet_overridesPreviousValue() {
        val activity1: Activity = mock()
        val activity2: Activity = mock()
        provider.set(activity1)
        provider.set(activity2)
        provider.get() shouldBe activity2
    }

    @Test
    fun testGet_getsActivityWithReflection_whenValueIsNotSet() {
        provider.set(null)

        activityScenarioRule.scenario.moveToState(Lifecycle.State.RESUMED)

        val result = provider.get()
        (result is FakeActivity) shouldBe true
    }
}