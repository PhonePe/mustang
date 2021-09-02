/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.growth.mustang.detail;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.detail.impl.EqualityDetail;
import com.phonepe.growth.mustang.detail.impl.RangeDetail;
import com.phonepe.growth.mustang.detail.impl.RegexDetail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "caveat")
@JsonSubTypes({ @JsonSubTypes.Type(name = Caveat.EQUALITY_TEXT, value = EqualityDetail.class),
        @JsonSubTypes.Type(name = Caveat.REGEX_TEXT, value = RegexDetail.class),
        @JsonSubTypes.Type(name = Caveat.RANGE_TEXT, value = RangeDetail.class), })
@JsonPropertyOrder({ "caveat", "values", "regex", "lowerBound", "upperBound", "includeLowerBound",
        "includeUpperBound" })
public abstract class Detail {
    @NotNull
    private final Caveat caveat;

    public abstract boolean validate(final RequestContext context, final Object lhsValue);

    public abstract <T> T accept(DetailVisitor<T> visitor);

}
