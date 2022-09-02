/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.mustang.detail;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.detail.impl.EqualityDetail;
import com.phonepe.mustang.detail.impl.RangeDetail;
import com.phonepe.mustang.detail.impl.RegexDetail;
import com.phonepe.mustang.detail.impl.VersioningDetail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "caveat")
@JsonSubTypes({ @JsonSubTypes.Type(name = Caveat.EQUALITY_TEXT, value = EqualityDetail.class),
        @JsonSubTypes.Type(name = Caveat.REGEX_TEXT, value = RegexDetail.class),
        @JsonSubTypes.Type(name = Caveat.RANGE_TEXT, value = RangeDetail.class),
        @JsonSubTypes.Type(name = Caveat.VERSIONING_TEXT, value = VersioningDetail.class) })
@JsonPropertyOrder({ "caveat", "values", "regex", "lowerBound", "upperBound", "includeLowerBound", "includeUpperBound",
        "check", "baseVersion", "excludeBase" })
public abstract class Detail {
    @NotNull
    private final Caveat caveat;

    public abstract boolean validate(final RequestContext context, final Object lhsValue);

    public abstract <T> T accept(DetailVisitor<T> visitor);

}
