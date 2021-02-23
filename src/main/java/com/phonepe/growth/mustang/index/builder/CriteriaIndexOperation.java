package com.phonepe.growth.mustang.index.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum CriteriaIndexOperation {
    ADD() {
        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visitAdd();
        }
    },
    UPDATE() {
        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visitUpdate();
        }
    },
    DELETE() {
        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visitDelete();
        }
    };

    protected abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visitAdd();

        T visitUpdate();

        T visitDelete();
    }
}
