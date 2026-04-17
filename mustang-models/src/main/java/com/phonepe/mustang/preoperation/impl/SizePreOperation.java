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

package com.phonepe.mustang.preoperation.impl;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.phonepe.mustang.preoperation.PreOperation;
import com.phonepe.mustang.preoperation.PreOperationType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SizePreOperation extends PreOperation {

    @Builder
    @JsonCreator
    public SizePreOperation(PreOperationType type) {
        super(PreOperationType.SIZE);
    }

    @Override
    public Object operate(Object value) {
        if (canApply(value)) {
            return ((List<?>) value).size();
        }
        return value;
    }

    @Override
    public boolean canApply(Object value) {
        return Objects.nonNull(value) && List.class.isAssignableFrom(value.getClass());
    }

}
