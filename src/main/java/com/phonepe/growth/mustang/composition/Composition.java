package com.phonepe.growth.mustang.composition;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.predicate.Predicate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(name = CompositionType.AND_TEXT, value = Conjunction.class),
        @JsonSubTypes.Type(name = CompositionType.OR_TEXT, value = Disjunction.class), })
public abstract class Composition {
    @NotNull
    private final CompositionType type;
    @Size(min = 2)
    private List<Predicate> predicates;

    public abstract boolean evaluate(RequestContext context);

    public abstract double getScore(RequestContext context);

    public abstract <T> T accept(CompositionVisitor<T> visitor);

    public int getWeigthFromContext(RequestContext context, Predicate predicate) {
        try {
            JsonPath.read(context.getNode()
                    .toString(), predicate.getLhs());
            return 1;
        } catch (PathNotFoundException e) {
            return 0;
        }
    }
}
