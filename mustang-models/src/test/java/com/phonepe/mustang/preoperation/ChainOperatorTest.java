/*
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonepe.mustang.preoperation;

import com.phonepe.mustang.preoperation.impl.AdditionPreOperation;
import com.phonepe.mustang.preoperation.impl.IdentityOperation;
import com.phonepe.mustang.preoperation.impl.MultiplicationPreOperation;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ChainOperatorTest {

    @Test
    public void testChainSingleIdentity() {
        List<PreOperation> ops = List.of(IdentityOperation.builder().build());
        Assert.assertEquals(10, ChainOperator.operate(ops, 10));
    }

    @Test
    public void testChainMultipleOperations() {
        // (10 + 5) * 2 = 30
        List<PreOperation> ops = Arrays.asList(
                AdditionPreOperation.builder().rhs(5).build(),
                MultiplicationPreOperation.builder().rhs(2).build()
        );
        Object result = ChainOperator.operate(ops, 10);
        Assert.assertEquals(0, BigDecimal.valueOf(30).compareTo((BigDecimal) result));
    }

    @Test
    public void testChainWithNull() {
        List<PreOperation> ops = List.of(IdentityOperation.builder().build());
        Assert.assertNull(ChainOperator.operate(ops, null));
    }

    @Test
    public void testChainEmptyOperations() {
        List<PreOperation> ops = List.of();
        Assert.assertEquals(10, ChainOperator.operate(ops, 10));
    }
}

