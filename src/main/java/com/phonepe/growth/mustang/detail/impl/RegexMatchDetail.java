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
package com.phonepe.growth.mustang.detail.impl;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.detail.Caveat;
import com.phonepe.growth.mustang.detail.Detail;
import com.phonepe.growth.mustang.detail.DetailVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RegexMatchDetail extends Detail {
    @NotBlank
    private String regex;

    @Builder
    @JsonCreator
    public RegexMatchDetail(@JsonProperty("regex") String regex) {
        super(Caveat.REGEX_MATCH);
        this.regex = regex;
    }

    @Override
    public boolean validate(RequestContext context, Object lhsValue) {
        return String.valueOf(lhsValue)
                .matches(regex);
    }

    @Override
    public <T> T accept(DetailVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
