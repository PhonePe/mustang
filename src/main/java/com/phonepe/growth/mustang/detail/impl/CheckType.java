package com.phonepe.growth.mustang.detail.impl;

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
    private String value;

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visitAbove();

        T visitBelow();
    }

}
