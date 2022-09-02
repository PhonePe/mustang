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
package com.phonepe.mustang.index.operation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum IndexOperation {
    ADD() {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAdd();
        }
    },
    UPDATE() {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitUpdate();
        }
    },
    DELETE() {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDelete();
        }
    };

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visitAdd();

        T visitUpdate();

        T visitDelete();
    }
}
