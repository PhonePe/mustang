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
package com.phonepe.mustang.criteria;

import lombok.Getter;

public enum CriteriaForm {
    DNF(CriteriaForm.DNF_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDNF();
        }
    },
    CNF(CriteriaForm.CNF_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitCNF();
        }
    };

    public static final String DNF_TEXT = "DNF"; // Disjunctive Normal Form
    public static final String CNF_TEXT = "CNF"; // Conjunctive Normal Form
    @Getter
    private final String value;

    private CriteriaForm(String value) {
        this.value = value;
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visitDNF();

        T visitCNF();
    }
}
