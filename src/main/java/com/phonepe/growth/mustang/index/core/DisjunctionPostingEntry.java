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
package com.phonepe.growth.mustang.index.core;

import com.phonepe.growth.mustang.predicate.PredicateType;

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
    private Integer order;
    private long score;

    @Override
    public int compareTo(DisjunctionPostingEntry o) {
        final int idc = iId.compareTo(o.getIId());
        if (idc != 0) {
            return idc;
        }
        final int tc = type.compareTo(o.getType());
        if (tc != 0) {
            return tc;
        }
        return order.compareTo(o.getOrder());
    }
}