package com.emarsys.core;

import com.emarsys.core.util.SystemUtils;

import org.junit.Assert;
import org.junit.Test;

public class SystemUtilsTest_withoutKotlin {

    @Test
    public void testIsKotlinEnabled_shouldBeFalse_inThisModule() {
        Assert.assertFalse(SystemUtils.isKotlinEnabled());
    }
}
