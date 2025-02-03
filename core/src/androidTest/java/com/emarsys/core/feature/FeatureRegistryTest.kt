package com.emarsys.core.feature

import com.emarsys.core.api.experimental.FlipperFeature
import com.emarsys.core.feature.FeatureRegistry.disableFeature
import com.emarsys.core.feature.FeatureRegistry.enableFeature
import com.emarsys.core.feature.FeatureRegistry.isFeatureEnabled
import com.emarsys.core.feature.FeatureRegistry.reset
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FeatureRegistryTest  {
    private lateinit var feature1: FlipperFeature
    private lateinit var feature2: FlipperFeature
    private lateinit var feature3: FlipperFeature
    private lateinit var features: List<FlipperFeature>


    @Before
    fun setUp() {
        reset()
        feature1 = mock()
        whenever(feature1.featureName).thenReturn("feature1")
        feature2 = mock()
        whenever(feature2.featureName).thenReturn("feature2")
        feature3 = mock()
        whenever(feature3.featureName).thenReturn("feature3")
        features = listOf(feature1, feature2, feature3)
    }

    @After
    fun tearDown() {
        reset()
    }

    @Test
    fun testIsFeatureEnabled_shouldDefaultToBeingTurnedOff() {
        for (feature in features) {
            isFeatureEnabled(feature) shouldBe false
        }
    }

    @Test
    fun testIsFeatureEnabled_shouldReturnTrue_whenFeatureIsTurnedOn() {
        enableFeature(feature1)
        isFeatureEnabled(feature1) shouldBe true
    }

    @Test
    fun testEnableFeature_shouldAppendFeaturesToTheEnabledFeatureSet() {
        FeatureRegistry.enabledFeatures.size shouldBe 0
        enableFeature(feature1)
        enableFeature(feature2)
        FeatureRegistry.enabledFeatures.size shouldBe 2
    }

    @Test
    fun testDisableFeature_shouldRemoveFeaturesFromTheEnabledFeatureSet() {
        enableFeature(feature1)
        enableFeature(feature2)
        FeatureRegistry.enabledFeatures.size shouldBe 2
        disableFeature(feature1)
        FeatureRegistry.enabledFeatures.size shouldBe 1
    }

    @Test
    fun testReset_shouldRemoveAllFeaturesFromTheEnabledFeatureSet() {
        enableFeature(feature1)
        enableFeature(feature2)
        FeatureRegistry.enabledFeatures.size shouldBe 2
        reset()
        FeatureRegistry.enabledFeatures.size shouldBe 0
    }
}