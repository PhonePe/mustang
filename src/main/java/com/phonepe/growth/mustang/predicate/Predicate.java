package com.phonepe.growth.mustang.predicate;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(name = PredicateType.INCLUDED_TEXT, value = IncludedPredicate.class),
        @JsonSubTypes.Type(name = PredicateType.EXCLUDED_TEXT, value = ExcludedPredicate.class), })
@JsonPropertyOrder({ "type", "lhs", "values", "lhsNotAPath", "weight", "defaultResult" })
public abstract class Predicate {
    @NotNull
    private PredicateType type;
    @NotBlank
    private String lhs;
    private boolean lhsNotAPath;
    private long weight;
    private boolean defaultResult;

    public boolean evaluate(RequestContext context) {
        if (lhsNotAPath) {
            return evaluate(context, lhs);
        }
        try {
            return evaluate(context,
                    JsonPath.read(context.getNode()
                            .toString(), lhs));
        } catch (PathNotFoundException e) {
            return defaultResult;
        }
    }

    public abstract <T> T accept(PredicateVisitor<T> visitor);

    protected abstract boolean evaluate(RequestContext context, Object lhsValue);

}
