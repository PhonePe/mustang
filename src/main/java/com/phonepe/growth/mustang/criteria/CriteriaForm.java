package com.phonepe.growth.mustang.criteria;

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
    private String value;

    private CriteriaForm(String value) {
        this.value = value;
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visitDNF();

        T visitCNF();
    }
}
