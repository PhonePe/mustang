package com.phonepe.central.mustang.detail.impl;

import org.junit.Assert;
import org.junit.Test;

public class VersioningDetailTest {

    @Test
    public void testValidateAboveVersion() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("1.0.0")
                .build();
        Assert.assertTrue(detail.validate("2.0.0"));
    }

    @Test
    public void testValidateAboveVersionEqual() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("1.0.0")
                .build();
        Assert.assertTrue(detail.validate("1.0.0"));
    }

    @Test
    public void testValidateAboveVersionExcludeBase() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("1.0.0")
                .excludeBase(true)
                .build();
        Assert.assertFalse(detail.validate("1.0.0"));
        Assert.assertTrue(detail.validate("1.0.1"));
    }

    @Test
    public void testValidateAboveVersionBelow() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("2.0.0")
                .build();
        Assert.assertFalse(detail.validate("1.0.0"));
    }

    @Test
    public void testValidateBelowVersion() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.BELOW)
                .baseVersion("2.0.0")
                .build();
        Assert.assertTrue(detail.validate("1.0.0"));
    }

    @Test
    public void testValidateBelowVersionEqual() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.BELOW)
                .baseVersion("1.0.0")
                .build();
        Assert.assertTrue(detail.validate("1.0.0"));
    }

    @Test
    public void testValidateBelowVersionExcludeBase() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.BELOW)
                .baseVersion("1.0.0")
                .excludeBase(true)
                .build();
        Assert.assertFalse(detail.validate("1.0.0"));
        Assert.assertTrue(detail.validate("0.9.0"));
    }

    @Test
    public void testValidateWithNull() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("1.0.0")
                .build();
        Assert.assertFalse(detail.validate(null));
    }

    @Test
    public void testNormalisedViewAndReconstruction() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("1.2.3")
                .excludeBase(true)
                .build();
        String normalised = detail.getNormalisedView();
        VersioningDetail reconstructed = VersioningDetail.of(normalised);
        Assert.assertEquals(detail.getCheck(), reconstructed.getCheck());
        Assert.assertEquals(detail.getBaseVersion(), reconstructed.getBaseVersion());
        Assert.assertEquals(detail.isExcludeBase(), reconstructed.isExcludeBase());
    }

    @Test
    public void testValidateAlphaVersion() {
        VersioningDetail detail = VersioningDetail.builder()
                .check(CheckType.ABOVE)
                .baseVersion("1.2.3.4-alpha")
                .build();
        Assert.assertTrue(detail.validate("1.2.3.4"));
    }
}

