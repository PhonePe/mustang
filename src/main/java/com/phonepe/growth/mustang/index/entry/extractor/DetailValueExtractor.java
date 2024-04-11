/**
 * Copyright (c) 2022 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.index.entry.extractor;

import java.util.Collections;
import java.util.Set;

import com.phonepe.growth.mustang.detail.DetailVisitor;
import com.phonepe.growth.mustang.detail.impl.EqualityDetail;
import com.phonepe.growth.mustang.detail.impl.RangeDetail;
import com.phonepe.growth.mustang.detail.impl.RegexDetail;
import com.phonepe.growth.mustang.detail.impl.VersioningDetail;

public final class DetailValueExtractor implements DetailVisitor<Set<Object>> {

    @Override
    public Set<Object> visit(EqualityDetail detail) {
        return detail.getValues();
    }

    @Override
    public Set<Object> visit(RegexDetail detail) {
        return Collections.singleton(detail.getRegex());
    }

    @Override
    public Set<Object> visit(RangeDetail detail) {
        return Collections.singleton(detail.getNormalisedView());
    }

    @Override
    public Set<Object> visit(VersioningDetail detail) {
        return Collections.singleton(detail.getNormalisedView());
    }

}