package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Objects;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.preoperation.PreOperation;
import com.phonepe.growth.mustang.preoperation.PreOperationType;
import com.phonepe.growth.mustang.preoperation.PreOperationVisitor;

import io.dropwizard.validation.ValidationMethod;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SubStringPreOperation extends PreOperation {
    @PositiveOrZero
    private int beginIndex;
    @Positive
    private int endIndex;

    @Builder
    @JsonCreator
    public SubStringPreOperation(@JsonProperty("beginIndex") int beginIndex, @JsonProperty("endIndex") int endIndex) {
        super(PreOperationType.SUBSTRING);
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
    }

    @Override
    public <T> T accept(PreOperationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Object operate(Object value) {
        if (Objects.nonNull(value) && String.class.isAssignableFrom(value.getClass())) {
            return value.toString()
                    .substring(beginIndex, endIndex);
        }
        return value;
    }

    @JsonIgnore
    @ValidationMethod
    public boolean isValid() {
        return beginIndex < endIndex;
    }

}
