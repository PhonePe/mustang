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

import java.util.Calendar;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.mustang.preoperation.PreOperation;
import com.phonepe.mustang.preoperation.PreOperationType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DateTimePreOperation extends PreOperation {

    @NotNull
    private DateExtractionType extract;

    @Builder
    @JsonCreator
    public DateTimePreOperation(@JsonProperty("extract") DateExtractionType extract) {
        super(PreOperationType.DATETIME);
        this.extract = extract;
    }

    @Override
    public Object operate(Object lhs) {
        if (canApply(lhs)) {
            final Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(((Number) lhs).longValue());
            return extract.accept(DateExtractionImpl.builder()
                    .instance(instance)
                    .build());
        }
        return lhs;
    }

    @Override
    public boolean canApply(Object lhs) {
        return Objects.nonNull(lhs) && Number.class.isAssignableFrom(lhs.getClass());
    }

}
