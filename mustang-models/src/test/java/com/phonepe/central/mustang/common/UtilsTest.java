package com.phonepe.central.mustang.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCheckExistenceWithNonNull() {
        Assert.assertTrue(Utils.checkExistence("value"));
    }

    @Test
    public void testCheckExistenceWithNull() {
        Assert.assertFalse(Utils.checkExistence(null));
    }

    @Test
    public void testGetRationalWeightWithNull() {
        Assert.assertEquals(Long.valueOf(1L), Utils.getRationalWeight(null));
    }

    @Test
    public void testGetRationalWeightWithZero() {
        Assert.assertEquals(Long.valueOf(1L), Utils.getRationalWeight(0L));
    }

    @Test
    public void testGetRationalWeightWithPositive() {
        Assert.assertEquals(Long.valueOf(5L), Utils.getRationalWeight(5L));
    }

    @Test
    public void testGetRationalWeightWithNegative() {
        Assert.assertEquals(Long.valueOf(-3L), Utils.getRationalWeight(-3L));
    }

    @Test
    public void testCompareStrings() {
        Assert.assertTrue(Utils.compare("hello", "hello"));
        Assert.assertFalse(Utils.compare("hello", "world"));
    }

    @Test
    public void testCompareNumbers() {
        Assert.assertTrue(Utils.compare(1, 1.0));
        Assert.assertTrue(Utils.compare(3.14, 3.14));
        Assert.assertFalse(Utils.compare(1, 2.0));
    }

    @Test
    public void testCompareBooleans() {
        Assert.assertTrue(Utils.compare(true, true));
        Assert.assertTrue(Utils.compare(false, false));
        Assert.assertFalse(Utils.compare(true, false));
    }

    @Test
    public void testCompareWithNullLhs() {
        // When lhsValue is null, falls through to rhsValue.equals(lhsValue)
        Assert.assertFalse(Utils.compare(null, "hello"));
    }

    @Test
    public void testCompareWithCollection() {
        List<Object> list = new ArrayList<>();
        list.add("A1");
        Assert.assertTrue(Utils.compare(list, "A1"));
    }

    @Test
    public void testIsSubSetTrue() {
        List<Object> lhs = Arrays.asList("A", "B");
        Set<Object> rhs = new HashSet<>(Arrays.asList("A", "B", "C"));
        Assert.assertTrue(Utils.isSubSet(lhs, rhs));
    }

    @Test
    public void testIsSubSetFalse() {
        List<Object> lhs = Arrays.asList("A", "D");
        Set<Object> rhs = new HashSet<>(Arrays.asList("A", "B", "C"));
        Assert.assertFalse(Utils.isSubSet(lhs, rhs));
    }

    @Test
    public void testIsSubSetNonCollection() {
        Assert.assertFalse(Utils.isSubSet("A", new HashSet<>(Arrays.asList("A", "B"))));
    }

    @Test
    public void testAreEqualSetsTrue() {
        List<Object> lhs = Arrays.asList("A", "B");
        Set<Object> rhs = new HashSet<>(Arrays.asList("A", "B"));
        Assert.assertTrue(Utils.areEqualSets(lhs, rhs));
    }

    @Test
    public void testAreEqualSetsFalse() {
        List<Object> lhs = Arrays.asList("A", "B");
        Set<Object> rhs = new HashSet<>(Arrays.asList("A", "B", "C"));
        Assert.assertFalse(Utils.areEqualSets(lhs, rhs));
    }

    @Test
    public void testAreEqualSetsNonCollection() {
        Assert.assertFalse(Utils.areEqualSets("A", new HashSet<>(Arrays.asList("A"))));
    }

    @Test
    public void testIsSuperSetTrue() {
        List<Object> lhs = Arrays.asList("A", "B", "C");
        Set<Object> rhs = new HashSet<>(Arrays.asList("A", "B"));
        Assert.assertTrue(Utils.isSuperSet(lhs, rhs));
    }

    @Test
    public void testIsSuperSetFalse() {
        List<Object> lhs = Arrays.asList("A");
        Set<Object> rhs = new HashSet<>(Arrays.asList("A", "B"));
        Assert.assertFalse(Utils.isSuperSet(lhs, rhs));
    }

    @Test
    public void testIsSuperSetNonCollection() {
        Assert.assertFalse(Utils.isSuperSet("A", new HashSet<>(Arrays.asList("A"))));
    }

    @Test
    public void testGetNodeValue() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        JsonNode node = mapper.valueToTree(data);
        Assert.assertEquals("value", Utils.getNodeValue(node, "$.key"));
    }

    @Test
    public void testGetNodeValueMissingPath() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        JsonNode node = mapper.valueToTree(data);
        Assert.assertNull(Utils.getNodeValue(node, "$.missing"));
    }

    @Test
    public void testGetNodeValueWithNonEmptyList() {
        Map<String, Object> data = new HashMap<>();
        data.put("items", Arrays.asList("A", "B", "C"));
        JsonNode node = mapper.valueToTree(data);
        Object result = Utils.getNodeValue(node, "$.items");
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof List);
        Assert.assertEquals(3, ((List<?>) result).size());
    }

    @Test
    public void testGetNodeValueWithEmptyList() {
        Map<String, Object> data = new HashMap<>();
        data.put("items", Collections.emptyList());
        JsonNode node = mapper.valueToTree(data);
        Object result = Utils.getNodeValue(node, "$.items");
        Assert.assertNull(result);
    }

    @Test
    public void testConstants() {
        Assert.assertEquals("AD", Utils.Constants.ERA_AD);
        Assert.assertEquals("BC", Utils.Constants.ERA_BC);
        Assert.assertEquals("AM", Utils.Constants.AM);
        Assert.assertEquals("PM", Utils.Constants.PM);
    }

    @Test
    public void testMarker() {
        Assert.assertEquals(Collections.singleton("-"), Utils.MARKER);
    }

    @Test
    public void testDefaultPreOperation() {
        Assert.assertNotNull(Utils.DEFAULT_PREOPERATION);
        Assert.assertEquals(1, Utils.DEFAULT_PREOPERATION.size());
    }
}

