/**
 * Copyright (c) 2022 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phonepe.growth.mustang.detail.impl;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.detail.Caveat;
import com.phonepe.growth.mustang.detail.Detail;
import com.phonepe.growth.mustang.detail.DetailVisitor;

import io.dropwizard.validation.ValidationMethod;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RegexDetail extends Detail {
    @NotBlank
    private String regex;

    @Builder
    @JsonCreator
    public RegexDetail(@JsonProperty("regex") String regex) {
        super(Caveat.REGEX);
        this.regex = regex;
    }

    @Override
    public boolean validate(Object lhsValue) {
        if (Objects.nonNull(lhsValue) && String.class.isAssignableFrom(lhsValue.getClass())) {
            return lhsValue.toString()
                    .matches(regex);
        }
        return false;
    }

    @JsonIgnore
    @ValidationMethod(message = "Regex string is invalid")
    public boolean isValidRegexDetail() {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    @Override
    public <T> T accept(DetailVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
