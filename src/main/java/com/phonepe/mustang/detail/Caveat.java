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
package com.phonepe.mustang.detail;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Caveat {
    NONE(Caveat.NONE_TEXT) {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitNone();
        }
    },
    EQUALITY(Caveat.EQUALITY_TEXT) {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitEquality();
        }
    },
    REGEX(Caveat.REGEX_TEXT) {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitRegexMatch();
        }
    },
    RANGE(Caveat.RANGE_TEXT) {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitRange();
        }
    },
    VERSIONING(Caveat.VERSIONING_TEXT) {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitVersioning();
        }
    };

    @Getter
    private final String value;

    public static final String NONE_TEXT = "TEXT";
    public static final String EQUALITY_TEXT = "EQUALITY";
    public static final String REGEX_TEXT = "REGEX";
    public static final String RANGE_TEXT = "RANGE";
    public static final String VERSIONING_TEXT = "VERSIONING";

    public abstract <T> T visit(Visitor<T> visitor);

    public interface Visitor<T> {

        T visitNone();

        T visitEquality();

        T visitRegexMatch();

        T visitRange();

        T visitVersioning();

    }
}
