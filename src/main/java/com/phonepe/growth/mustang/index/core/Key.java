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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.phonepe.growth.mustang.detail.Caveat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "name", "caveat", "value", "order" })
@NoArgsConstructor
@AllArgsConstructor
public class Key {
    @NotBlank
    private String name;
    @NotNull
    private Caveat caveat;
    @NotNull
    private Object value;
    private int order;
    @Builder.Default
    private long upperBoundScore = 10;
}
