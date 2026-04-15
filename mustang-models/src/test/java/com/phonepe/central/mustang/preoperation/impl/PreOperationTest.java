package com.phonepe.central.mustang.preoperation.impl;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

public class PreOperationTest {

    // --- IdentityOperation ---
    @Test
    public void testIdentityOperate() {
        IdentityOperation op = IdentityOperation.builder().build();
        Assert.assertEquals("hello", op.operate("hello"));
        Assert.assertEquals(42, op.operate(42));
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testIdentityCanApply() {
        IdentityOperation op = IdentityOperation.builder().build();
        Assert.assertTrue(op.canApply("anything"));
        Assert.assertTrue(op.canApply(null));
    }

    // --- AdditionPreOperation ---
    @Test
    public void testAdditionOperate() {
        AdditionPreOperation op = AdditionPreOperation.builder().rhs(5).build();
        Object result = op.operate(10);
        Assert.assertEquals(0, BigDecimal.valueOf(15).compareTo((BigDecimal) result));
    }

    @Test
    public void testAdditionOperateWithNull() {
        AdditionPreOperation op = AdditionPreOperation.builder().rhs(5).build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testAdditionOperateWithNonNumber() {
        AdditionPreOperation op = AdditionPreOperation.builder().rhs(5).build();
        Assert.assertEquals("hello", op.operate("hello"));
    }

    @Test
    public void testAdditionCanApply() {
        AdditionPreOperation op = AdditionPreOperation.builder().rhs(5).build();
        Assert.assertTrue(op.canApply(10));
        Assert.assertFalse(op.canApply("text"));
        Assert.assertFalse(op.canApply(null));
    }

    // --- SubtractionPreOperation ---
    @Test
    public void testSubtractionOperate() {
        SubtractionPreOperation op = SubtractionPreOperation.builder().rhs(3).build();
        Object result = op.operate(10);
        Assert.assertEquals(0, BigDecimal.valueOf(7).compareTo((BigDecimal) result));
    }

    @Test
    public void testSubtractionOperateWithNull() {
        SubtractionPreOperation op = SubtractionPreOperation.builder().rhs(3).build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testSubtractionCanApply() {
        SubtractionPreOperation op = SubtractionPreOperation.builder().rhs(3).build();
        Assert.assertTrue(op.canApply(10));
        Assert.assertFalse(op.canApply("text"));
    }

    // --- MultiplicationPreOperation ---
    @Test
    public void testMultiplicationOperate() {
        MultiplicationPreOperation op = MultiplicationPreOperation.builder().rhs(4).build();
        Object result = op.operate(3);
        Assert.assertEquals(0, BigDecimal.valueOf(12).compareTo((BigDecimal) result));
    }

    @Test
    public void testMultiplicationOperateWithNull() {
        MultiplicationPreOperation op = MultiplicationPreOperation.builder().rhs(4).build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testMultiplicationCanApply() {
        MultiplicationPreOperation op = MultiplicationPreOperation.builder().rhs(4).build();
        Assert.assertTrue(op.canApply(3));
        Assert.assertFalse(op.canApply("text"));
    }

    // --- DivisionPreOperation ---
    @Test
    public void testDivisionOperate() {
        DivisionPreOperation op = DivisionPreOperation.builder().rhs(3).build();
        Assert.assertEquals(3L, op.operate(10));
    }

    @Test
    public void testDivisionOperateWithZeroRhs() {
        DivisionPreOperation op = DivisionPreOperation.builder().rhs(0).build();
        Assert.assertEquals(10, op.operate(10));
    }

    @Test
    public void testDivisionOperateWithNull() {
        DivisionPreOperation op = DivisionPreOperation.builder().rhs(3).build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testDivisionCanApply() {
        DivisionPreOperation op = DivisionPreOperation.builder().rhs(3).build();
        Assert.assertTrue(op.canApply(10));
        Assert.assertFalse(op.canApply("text"));
        Assert.assertFalse(op.canApply(null));
    }

    @Test
    public void testDivisionCanApplyZeroRhs() {
        DivisionPreOperation op = DivisionPreOperation.builder().rhs(0).build();
        Assert.assertFalse(op.canApply(10));
    }

    // --- ModuloPreOperation ---
    @Test
    public void testModuloOperate() {
        ModuloPreOperation op = ModuloPreOperation.builder().rhs(3).build();
        Assert.assertEquals(1L, op.operate(10));
    }

    @Test
    public void testModuloOperateWithZeroRhs() {
        ModuloPreOperation op = ModuloPreOperation.builder().rhs(0).build();
        Assert.assertEquals(10, op.operate(10));
    }

    @Test
    public void testModuloOperateWithNull() {
        ModuloPreOperation op = ModuloPreOperation.builder().rhs(3).build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testModuloCanApply() {
        ModuloPreOperation op = ModuloPreOperation.builder().rhs(3).build();
        Assert.assertTrue(op.canApply(10));
        Assert.assertFalse(op.canApply("text"));
    }

    @Test
    public void testModuloCanApplyZeroRhs() {
        ModuloPreOperation op = ModuloPreOperation.builder().rhs(0).build();
        Assert.assertFalse(op.canApply(10));
    }

    // --- LengthPreOperation ---
    @Test
    public void testLengthOperate() {
        LengthPreOperation op = LengthPreOperation.builder().build();
        Assert.assertEquals(5, op.operate("hello"));
    }

    @Test
    public void testLengthOperateWithNull() {
        LengthPreOperation op = LengthPreOperation.builder().build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testLengthOperateWithNonString() {
        LengthPreOperation op = LengthPreOperation.builder().build();
        Assert.assertEquals(42, op.operate(42));
    }

    @Test
    public void testLengthCanApply() {
        LengthPreOperation op = LengthPreOperation.builder().build();
        Assert.assertTrue(op.canApply("hello"));
        Assert.assertFalse(op.canApply(42));
        Assert.assertFalse(op.canApply(null));
    }

    // --- SubStringPreOperation ---
    @Test
    public void testSubStringOperate() {
        SubStringPreOperation op = SubStringPreOperation.builder().beginIndex(1).endIndex(4).build();
        Assert.assertEquals("ell", op.operate("hello"));
    }

    @Test
    public void testSubStringOperateWithNull() {
        SubStringPreOperation op = SubStringPreOperation.builder().beginIndex(0).endIndex(3).build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testSubStringOperateWithNonString() {
        SubStringPreOperation op = SubStringPreOperation.builder().beginIndex(0).endIndex(3).build();
        Assert.assertEquals(42, op.operate(42));
    }

    @Test
    public void testSubStringCanApply() {
        SubStringPreOperation op = SubStringPreOperation.builder().beginIndex(0).endIndex(3).build();
        Assert.assertTrue(op.canApply("hello"));
        Assert.assertFalse(op.canApply(42));
        Assert.assertFalse(op.canApply(null));
    }

    @Test
    public void testSubStringIsValidTrue() {
        SubStringPreOperation op = SubStringPreOperation.builder().beginIndex(0).endIndex(3).build();
        Assert.assertTrue(op.isValid());
    }

    @Test
    public void testSubStringIsValidFalse() {
        SubStringPreOperation op = SubStringPreOperation.builder().beginIndex(5).endIndex(3).build();
        Assert.assertFalse(op.isValid());
    }

    @Test
    public void testSubStringCanApplyInvalidIndices() {
        SubStringPreOperation op = SubStringPreOperation.builder().beginIndex(5).endIndex(3).build();
        Assert.assertFalse(op.canApply("hello"));
    }

    // --- SizePreOperation ---
    @Test
    public void testSizeOperate() {
        SizePreOperation op = SizePreOperation.builder().build();
        Assert.assertEquals(3, op.operate(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void testSizeOperateWithNull() {
        SizePreOperation op = SizePreOperation.builder().build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testSizeOperateWithNonList() {
        SizePreOperation op = SizePreOperation.builder().build();
        Assert.assertEquals("hello", op.operate("hello"));
    }

    @Test
    public void testSizeCanApply() {
        SizePreOperation op = SizePreOperation.builder().build();
        Assert.assertTrue(op.canApply(Arrays.asList("a")));
        Assert.assertFalse(op.canApply("text"));
        Assert.assertFalse(op.canApply(null));
    }

    // --- BinaryConversionPreOperation ---
    @Test
    public void testBinaryConversionOperate() {
        BinaryConversionPreOperation op = BinaryConversionPreOperation.builder().build();
        String result = (String) op.operate(5);
        Assert.assertTrue(result.endsWith("101"));
        Assert.assertEquals(32, result.length());
    }

    @Test
    public void testBinaryConversionOperateWithNull() {
        BinaryConversionPreOperation op = BinaryConversionPreOperation.builder().build();
        Assert.assertNull(op.operate(null));
    }

    @Test
    public void testBinaryConversionOperateWithNonInteger() {
        BinaryConversionPreOperation op = BinaryConversionPreOperation.builder().build();
        Assert.assertEquals("hello", op.operate("hello"));
    }

    @Test
    public void testBinaryConversionCanApply() {
        BinaryConversionPreOperation op = BinaryConversionPreOperation.builder().build();
        Assert.assertTrue(op.canApply(5));
        Assert.assertFalse(op.canApply(5.0));
        Assert.assertFalse(op.canApply("text"));
        Assert.assertFalse(op.canApply(null));
    }
}

