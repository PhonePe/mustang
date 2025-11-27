package com.phonepe.growth.mustang.preoperation.impl;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.validation.constraints.NotEmpty;

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
public class ToDateTimePreOperation extends PreOperation {
    @NotEmpty
    private String dateTimeFormat;

    @Builder
    @JsonCreator
    public ToDateTimePreOperation(@JsonProperty("dateTimeFormat") String dateTimeFormat) {
        super(PreOperationType.TO_DATETIME);
        this.dateTimeFormat = dateTimeFormat;
    }

    @Override
    public Object operate(Object lhs) {
        if (canApply(lhs)) {
            try {
                return getFormatter().parse(lhs.toString());
            } catch (Exception e) {
            }
        }
        return lhs;
    }

    private DateTimeFormatter getFormatter() {
        try {
            return DateTimeFormatter.ofPattern(dateTimeFormat);
        } catch (Exception e) {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        }
    }

    @Override
    public boolean canApply(Object lhs) {
        return Objects.nonNull(lhs)
                && (String.class.isAssignableFrom(lhs.getClass()) || Number.class.isAssignableFrom(lhs.getClass()));
    }

}
