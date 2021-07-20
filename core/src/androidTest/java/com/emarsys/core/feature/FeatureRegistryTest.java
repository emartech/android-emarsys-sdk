package com.emarsys.core.feature;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureRegistryTest {

    private FlipperFeature feature1;
    private FlipperFeature feature2;
    private FlipperFeature feature3;
    private List<FlipperFeature> features;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        FeatureRegistry.reset();

        feature1 = mock(FlipperFeature.class);
        when(feature1.getFeatureName()).thenReturn("feature1");

        feature2 = mock(FlipperFeature.class);
        when(feature2.getFeatureName()).thenReturn("feature2");

        feature3 = mock(FlipperFeature.class);
        when(feature3.getFeatureName()).thenReturn("feature3");

        features = Arrays.asList(feature1, feature2, feature3);
    }

    @After
    public void tearDown() {
        FeatureRegistry.reset();
    }

    @Test
    public void testIsFeatureEnabled_shouldDefaultToBeingTurnedOff() {
        for (FlipperFeature feature : features) {
            assertFalse(FeatureRegistry.isFeatureEnabled(feature));
        }
    }

    @Test
    public void testIsFeatureEnabled_shouldReturnTrue_whenFeatureIsTurnedOn() {
        FeatureRegistry.enableFeature(feature1);
        assertTrue(FeatureRegistry.isFeatureEnabled(feature1));
    }

    @Test
    public void testEnableFeature_shouldAppendFeaturesToTheEnabledFeatureSet() {
        assertEquals(0, FeatureRegistry.enabledFeatures.size());
        FeatureRegistry.enableFeature(feature1);
        FeatureRegistry.enableFeature(feature2);
        assertEquals(2, FeatureRegistry.enabledFeatures.size());
    }

    @Test
    public void testDisableFeature_shouldRemoveFeaturesFromTheEnabledFeatureSet() {
        FeatureRegistry.enableFeature(feature1);
        FeatureRegistry.enableFeature(feature2);
        assertEquals(2, FeatureRegistry.enabledFeatures.size());
        FeatureRegistry.disableFeature(feature1);
        assertEquals(1, FeatureRegistry.enabledFeatures.size());
    }

    @Test
    public void testReset_shouldRemoveAllFeaturesFromTheEnabledFeatureSet() {
        FeatureRegistry.enableFeature(feature1);
        FeatureRegistry.enableFeature(feature2);
        assertEquals(2, FeatureRegistry.enabledFeatures.size());
        FeatureRegistry.reset();
        assertEquals(0, FeatureRegistry.enabledFeatures.size());
    }

}