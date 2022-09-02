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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CheckType {
    ABOVE(CheckType.ABOVE_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAbove();
        }
    },
    BELOW(CheckType.BELOW_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitBelow();
        }
    };

    public static final String ABOVE_TEXT = "ABOVE";
    public static final String BELOW_TEXT = "BELOW";

    @Getter
    private final String value;

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visitAbove();

        T visitBelow();
    }

}
