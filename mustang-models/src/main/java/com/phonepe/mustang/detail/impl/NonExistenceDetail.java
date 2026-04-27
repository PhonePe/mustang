/**
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
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
package com.phonepe.mustang.detail.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.phonepe.mustang.common.Utils;
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
public class NonExistenceDetail extends Detail {

    @Builder
    @JsonCreator
    public NonExistenceDetail() {
        super(Caveat.NON_EXISTENCE);
    }

    @Override
    public boolean validate(Object lhsValue) {
        return !Utils.checkExistence(lhsValue);
    }

    @Override
    public <T> T accept(DetailVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
