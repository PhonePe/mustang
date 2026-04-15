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
package com.phonepe.central.mustang.detail;

import com.phonepe.central.mustang.detail.impl.EqualSetDetail;
import com.phonepe.central.mustang.detail.impl.EqualityDetail;
import com.phonepe.central.mustang.detail.impl.ExistenceDetail;
import com.phonepe.central.mustang.detail.impl.NonExistenceDetail;
import com.phonepe.central.mustang.detail.impl.RangeDetail;
import com.phonepe.central.mustang.detail.impl.RegexDetail;
import com.phonepe.central.mustang.detail.impl.SubSetDetail;
import com.phonepe.central.mustang.detail.impl.SuperSetDetail;
import com.phonepe.central.mustang.detail.impl.VersioningDetail;

public interface DetailVisitor<T> {

    T visit(ExistenceDetail detail);

    T visit(NonExistenceDetail detail);

    T visit(EqualityDetail detail);

    T visit(SubSetDetail detail);

    T visit(EqualSetDetail detail);

    T visit(SuperSetDetail detail);

    T visit(RegexDetail detail);

    T visit(RangeDetail detail);

    T visit(VersioningDetail detail);

}
