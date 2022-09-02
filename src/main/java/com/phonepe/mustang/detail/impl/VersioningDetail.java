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
package com.phonepe.mustang.detail.impl;

import javax.validation.constraints.NotNull;

import org.apache.maven.artifact.versioning.ComparableVersion;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.detail.Caveat;
import com.phonepe.mustang.detail.Detail;
import com.phonepe.mustang.detail.DetailVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VersioningDetail extends Detail {
    private static final String NORMALISED_FORMAT = "%s#%s#%s";
    private static final String NORMALISED_FORMAT_SEPARATOR = "#";
    @NotNull
    private CheckType check;
    @NotBlank
    private String baseVersion;
    private boolean excludeBase;

    @Builder
    @JsonCreator
    public VersioningDetail(@JsonProperty("check") CheckType check,
            @JsonProperty("baseVersion") String baseVersion,
            @JsonProperty("excludeBase") boolean excludeBase) {
        super(Caveat.VERSIONING);
        this.baseVersion = baseVersion;
        this.check = check;
        this.excludeBase = excludeBase;
    }

    @Override
    public boolean validate(RequestContext context, Object lhsValue) {
        final int comparisionResult = new ComparableVersion(baseVersion)
                .compareTo(new ComparableVersion(lhsValue.toString()));
        return check.accept(new ComparisionInference(comparisionResult, excludeBase));
    }

    @Override
    public <T> T accept(DetailVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getNormalisedView() {
        return String.format(NORMALISED_FORMAT, check, baseVersion, excludeBase);
    }

    public static VersioningDetail of(final String normalisedView) {
        final String[] parts = normalisedView.split(NORMALISED_FORMAT_SEPARATOR);
        return VersioningDetail.builder()
                .check(CheckType.valueOf(parts[0]))
                .baseVersion(parts[1])
                .excludeBase(Boolean.valueOf(parts[2]))
                .build();

    }

}
