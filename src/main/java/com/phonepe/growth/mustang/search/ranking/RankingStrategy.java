package com.phonepe.growth.mustang.search.ranking;

import lombok.Getter;

public enum RankingStrategy {
    EXPLICIT_WEIGHTS(RankingStrategy.EXPLICIT_WEIGHTS_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitExplicitWeights();
        }
    },
    IMPLICIT_FREQUENCY(RankingStrategy.IMPLICIT_FREQUENCY_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitImplicitFrequency();
        }
    };

    public static final String EXPLICIT_WEIGHTS_TEXT = "EXPLICIT_WEIGHTS";
    public static final String IMPLICIT_FREQUENCY_TEXT = "IMPLICIT_FREQUENCY";

    @Getter
    private String value;

    RankingStrategy(String value) {
        this.value = value;
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visitExplicitWeights();

        T visitImplicitFrequency();
    }

}
