package com.phonepe.growth.mustang.predicate;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.jayway.jsonpath.JsonPath;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.growth.mustang.EvaluationContext;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = IncludedPredicate.class, name = PredicateType.INCLUDED_TEXT),
        @JsonSubTypes.Type(value = ExcludedPredicate.class, name = PredicateType.EXCLUDED_TEXT), })
public abstract class Predicate {
    @NotNull
    private PredicateType type;
    @NotBlank
    private String lhs;
    private boolean isLhsJsonPath;
    private boolean defaultResult;

    public boolean evaluate(EvaluationContext context) {
        if (isLhsJsonPath) {
            val nodeValue = JsonPath.read(context.getNode().toString(), lhs);
            if (Objects.isNull(nodeValue)) {
                return defaultResult;
            }
            return evaluate(context, nodeValue);

        }
        return evaluate(context, lhs);
    }

    public abstract <T> T accept(PredicateVisitor<T> visitor);

    protected abstract boolean evaluate(EvaluationContext context, Object lhsValue);

}
