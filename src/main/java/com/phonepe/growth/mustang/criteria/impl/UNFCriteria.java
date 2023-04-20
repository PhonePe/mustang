package com.phonepe.growth.mustang.criteria.impl;

import static com.phonepe.growth.mustang.predicate.Predicate.NO_MATCH_SCORE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.composition.CompositionType.Visitor;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.criteria.CriteriaVisitor;
import com.phonepe.growth.mustang.debug.DebugResult;
import com.phonepe.growth.mustang.debug.UNFDebugResult;
import com.phonepe.growth.mustang.predicate.Predicate;
import java.util.List;
import java.util.stream.DoubleStream;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.*;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UNFCriteria extends Criteria {

    @NotNull
    private final CompositionType type;

    @Valid
    @NotNull
    private final List<Criteria> criterias;

    @Valid
    @NotEmpty
    private final List<Predicate> predicates;


    @Builder
    @JsonCreator
    public UNFCriteria(@JsonProperty("id") final String id,
            @JsonProperty("type") final CompositionType type,
            @JsonProperty("criterias") @Singular final List<Criteria> criterias,
            @JsonProperty("predicates") @Singular final List<Predicate> predicates) {
        super(CriteriaForm.UNF, id);
        this.type = type;
        this.criterias = criterias;
        this.predicates = predicates;
    }

    @Override
    public boolean evaluate(RequestContext context) {
        return type.accept(new CompositionType.Visitor<>() {
            @Override
            public Boolean visitAnd() {
                return criterias.stream()
                        .allMatch(criteria -> criteria.evaluate(context)) && predicates.stream()
                        .allMatch(predicate -> predicate.evaluate(context));
            }

            @Override
            public Boolean visitOr() {
                return criterias.stream()
                        .anyMatch(criteria -> criteria.evaluate(context)) || predicates.stream()
                        .anyMatch(predicate -> predicate.evaluate(context));
            }
        });
    }

    @Override
    public DebugResult debug(RequestContext context) {
        return UNFDebugResult.UNFDebugResultBuilder()
                .result(evaluate(context))
                .id(this.getId())
                .form(this.getForm())
                .type(type)
                .predicateDebugResults(predicates.stream()
                        .map(predicate -> predicate.debug(context))
                        .toList())
                .debugResults(criterias.stream()
                        .map(criteria -> criteria.debug(context))
                        .toList())
                .build();
    }

    @Override
    public double getScore(RequestContext context) {
        return type.accept(new Visitor<>() {
            @Override
            public Double visitAnd() {
                double sum = 0.0;
                for (Criteria criteria : criterias) {
                    final double score = criteria.getScore(context);
                    if (score == NO_MATCH_SCORE) {
                        return NO_MATCH_SCORE;
                    }
                    sum += score;
                }
                for (Predicate predicate : predicates) {
                    final double score = predicate.getScore(context);
                    if (score == NO_MATCH_SCORE) {
                        return NO_MATCH_SCORE;
                    }
                    sum += score;
                }
                return sum;
            }

            @Override
            public Double visitOr() {
                return DoubleStream.concat(criterias.stream()
                                        .mapToDouble(criteria -> criteria.getScore(context)),
                                predicates.stream()
                                        .mapToDouble(predicate -> predicate.getScore(context)))
                        .max()
                        .orElse(0);
            }
        });
    }

    @Override
    public <T> T accept(CriteriaVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
