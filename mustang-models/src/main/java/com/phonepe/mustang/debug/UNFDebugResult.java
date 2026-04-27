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

package com.phonepe.mustang.debug;

import java.util.Collections;
import java.util.List;

import com.phonepe.mustang.composition.CompositionType;
import com.phonepe.mustang.criteria.CriteriaForm;

import lombok.Builder;
import lombok.Data;

@Data
public class UNFDebugResult extends DebugResult {

    private CompositionType type;
    private List<PredicateDebugResult> predicateDebugResults;
    private List<DebugResult> debugResults;

    @Builder(builderMethodName = "UNFDebugResultBuilder")
    public UNFDebugResult(boolean result,
            CriteriaForm form,
            String id,
            CompositionType type,
            List<PredicateDebugResult> predicateDebugResults,
            List<DebugResult> debugResults) {
        super(result, form, id, Collections.emptyList());
        this.type = type;
        this.predicateDebugResults = predicateDebugResults;
        this.debugResults = debugResults;
    }

}
