/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
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
package com.phonepe.mustang.index.core;

import com.phonepe.mustang.predicate.PredicateType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "iId", "type", "order" })
@NoArgsConstructor
@AllArgsConstructor
public class DisjunctionPostingEntry implements Comparable<DisjunctionPostingEntry> {
    private Integer iId;
    private String eId;
    private PredicateType type;
    private int order;
    private long score;

    @Override
    public int compareTo(DisjunctionPostingEntry o) {
        final int idc = iId.compareTo(o.getIId());
        if (idc != 0) {
            return idc;
        }
        return type.compareTo(o.getType());
    }
}