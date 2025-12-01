package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Calendar;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.preoperation.PreOperation;
import com.phonepe.growth.mustang.preoperation.PreOperationType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DateTimePreOperation extends PreOperation {

    @NotNull
    private DateExtracts extract;

    @Builder
    @JsonCreator
    public DateTimePreOperation(@JsonProperty("extract") DateExtracts extract) {
        super(PreOperationType.DATETIME);
        this.extract = extract;
    }

    @Override
    public Object operate(Object lhs) {
        if (canApply(lhs)) {
            final Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(((Number) lhs).longValue());
            return extract.accept(DateExtractImpl.builder()
                    .instance(instance)
                    .build());
        }
        return lhs;
    }

    @Override
    public boolean canApply(Object lhs) {
        return Objects.nonNull(lhs) && Number.class.isAssignableFrom(lhs.getClass());
    }

}
